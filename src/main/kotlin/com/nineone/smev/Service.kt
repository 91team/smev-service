package com.nineone.smev

import com.nineone.smev.schemas.*

import jakarta.xml.bind.JAXBContext
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.w3c.dom.Element
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import javax.xml.bind.Marshaller
import kotlin.concurrent.thread

class Service(private val client: Client) {
    private val producer: Producer<String, String> = createProducer()
    private val consumer: Consumer<String, String> = createConsumer()
    private val requestsTopic = System.getenv("KAFKA_REQUSTS_TOPIC") ?: "smev_requests"
    private val responsesTopic = System.getenv("KAFKA_RESPONSES_TOPIC") ?: "smev_responses"
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val prettyPrint = true

    fun run() {
        val tmpDir = Files.createTempDirectory("smev").toFile().absolutePath
        consumer.subscribe(listOf(requestsTopic))

        listenSmevQueue()

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))

            if (records.isEmpty) continue

            records.forEach { it ->
                try {
                    val request = parsePackage(it.value())

                    when (request.type) {
                        PackageType.REQUEST -> {
                            val response = ExchangePackage().apply {
                                id = request.id
                                type = PackageType.NOTIFY
                                action = PackageAction.MESSAGE_SENT
                                message = "сообщение отправлено в СМЭВ"
                            }
                            try {
                                val files = request.mtomList.mtomFile.toMutableList().map { mtomFile ->
                                    File(Paths.get(tmpDir, mtomFile.fileName).toString()).also { file ->
                                        file.writeBytes(mtomFile.value)
                                    }
                                }
                                client.sendRequest(request.content.any as Element, request.id, files.toMutableList())
                            } catch(e: java.lang.Exception) {
                                logger.error("SendRequestRequest ERROR: {}", e.toString())
                                response.apply {
                                    action = PackageAction.ERROR
                                    message = e.toString()
                                }
                            } finally {
                                consumer.commitSync()
                                sendPackage(response)
                            }
                        }
                        else -> {
                            val error = "ERROR unsupported package type '${request.type}' for request topic"
                            logger.error(error)
                            sendPackage(ExchangePackage().apply {
                                id = request.id
                                type = PackageType.REPLY
                                action = PackageAction.ERROR
                                message = error
                            })
                            consumer.commitSync()
                        }
                    }
                } catch (e: jakarta.xml.bind.UnmarshalException) {
                    logger.error("ERROR ${e.javaClass.name} invalid package: ${it.value()}")
                }
            }

        }
    }

    private fun listenSmevQueue() {
        var active = true
        var queueDelay = false
        val queueDelayTime: Long = 1000
        val queueDelayTime2: Long = 2000

        thread {
            while (active) {
                if (queueDelay) {
                    queueDelay = false
                    Thread.sleep(queueDelayTime)
                }

                try {
//                    active = false
                    val data = client.getResponse(null, null)
                    val originalMessageId = data.responseMessage?.response?.originalMessageId
                    val asyncProcessingStatus = data.responseMessage?.response?.senderProvidedResponseData?.asyncProcessingStatus
                    if (originalMessageId != null) {
                        sendPackage(ExchangePackage().apply {
                            id = originalMessageId
                            type = PackageType.REPLY
                            action = PackageAction.OK
                            content = ExchangePackage.Content().apply { any = client.objectToDocument(data).documentElement }
                        })
                        if (asyncProcessingStatus != null) {
                            sendPackage(ExchangePackage().apply {
                                id = originalMessageId
                                type = PackageType.REPLY
                                action = PackageAction.ERROR
//                                message = (asyncProcessingStatus.statusDetails ?: asyncProcessingStatus.statusCategory) as String?
                                content = ExchangePackage.Content().apply { any = client.objectToDocument(asyncProcessingStatus).documentElement }
                            })
                        }

                    } else {
                        queueDelay = true
                    }
                } catch (e: java.lang.Exception) {
                    logger.error("GetResponseRequest ERROR: {}", e)
                    Thread.sleep(queueDelayTime2)
                }
            }
        }
    }

    private fun sendPackage(response: ExchangePackage) {
        producer.send(ProducerRecord(responsesTopic, response.id, serializePackage(response)))
    }

    private fun createProducer(): Producer<String, String> {
        return KafkaProducer(mapOf(
            "bootstrap.servers" to (System.getenv("KAFKA_ADDRESS") ?: "localhost:9092"),
            "acks" to "all",
            "retries" to 0,
            "linger.ms" to 1,
            "key.serializer" to StringSerializer::class.java,
            "value.serializer" to StringSerializer::class.java
        ))
    }

    private fun createConsumer(): Consumer<String, String> {
        return KafkaConsumer(mapOf(
            "bootstrap.servers" to (System.getenv("KAFKA_ADDRESS") ?: "localhost:9092"),
            "group.id" to "smev-adapter",
            "key.deserializer" to StringDeserializer::class.java,
            "value.deserializer" to StringDeserializer::class.java
        ))
    }

    private fun parsePackage(data: String): ExchangePackage {
        return JAXBContext
            .newInstance(ExchangePackage::class.java)
            .createUnmarshaller()
            .unmarshal(data.reader()) as ExchangePackage
    }

    private fun serializePackage(data: ExchangePackage): String {
        val stream = ByteArrayOutputStream()
        JAXBContext.newInstance(ExchangePackage::class.java).createMarshaller().run {
            setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint)
            marshal(data, stream)
        }
        return stream.toString().also { stream.close() }
    }
}