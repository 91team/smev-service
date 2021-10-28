package com.nineone.smev

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import ru.it.smev.message_exchange.autogenerated.types.basic.v1_1.*
import ru.it.smev.message_exchange.autogenerated.service.v1_1.SMEVMessageExchangeService
import ru.it.smev.message_exchange.autogenerated.service.v1_1.SMEVMessageExchangePortType
import ru.it.smev.message_exchange.autogenerated.types.v1_1.*

import ru.voskhod.crypto.XMLTransformHelper
import ru.voskhod.smev.client.api.identification.impl.IdentificationServiceImpl
import ru.voskhod.smev.client.api.services.signature.Signer
import ru.voskhod.smev.client.api.signature.configuration.SignatureConfigurationImpl
import ru.voskhod.smev.client.api.signature.impl.SignerImpl
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.net.URL
import java.time.LocalDateTime
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.datatype.DatatypeFactory
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory

class SMEVService(schemaUrl: String, //указывается точка доступа СМЭВ 3
                  keyAlias: String, // наименование хранилища JCP
                  keyPassword: String, //пароль от хранилища
                  private val isTest: Boolean = false,
                  soapServiceName: String = "SMEVMessageExchangeService",
                  soapEndpointName: String = "SMEVMessageExchangeEndpoint",
                  private val prettyPrint: Boolean = false
) {
    private val schemaUrl = URL(schemaUrl)
    private val smevNamespaceUri: String = "urn://x-artefacts-smev-gov-ru/services/message-exchange/1.1"
    private val soapServiceQname: QName = QName(smevNamespaceUri, soapServiceName)
    private val soapEndpointQname: QName = QName(smevNamespaceUri, soapEndpointName)
    private var sign: Signer

    init {
        val signConfig = SignatureConfigurationImpl("JCP2", keyAlias, keyAlias, keyPassword, null)
        sign = SignerImpl(signConfig)
        sign.init(keyAlias, keyAlias, keyPassword)
    }

    @Throws(Exception::class)
    fun sendRequest() {
        val message = """<fedstat:PublicRequest xmlns:fedstat="urn://x-artefacts-fedstat-ru/services/public/1.0.5"><fedstat:ClassifiersRequest/></fedstat:PublicRequest>"""

        val sendReq = SenderProvidedRequestData().apply {
            id = "SIGNED_BY_CALLER" // id подписи
            messageID = IdentificationServiceImpl().generateUUID() // messageId запроса
            if (isTest) testMessage = Void() // признак тестового сообщения
            // nodeID("421") // имя ноды(необязательно)
            messagePrimaryContent = MessagePrimaryContent().apply {
                any = buildMessage(message)
            }
        }

        val reqParam = SendRequestRequest().apply {
            senderProvidedRequestData = sendReq
            callerInformationSystemSignature = signElement(senderProvidedRequestData)

            println("request = " + objectToString(this))
        }

        val smev = SMEVMessageExchangeService(schemaUrl, soapServiceQname)
        try {
            val port = smev.getPort(soapEndpointQname, SMEVMessageExchangePortType::class.java)
            port.sendRequest(reqParam).also { println("resp = " + objectToString(it)) }
        } catch (e: java.lang.Exception) {
            println("ERROR ${e.javaClass.name}: ${e.message}")
        }
    }

    @Throws(Exception::class)
    fun getResponse() {
        val message = MessageTypeSelector().apply {
            id = "SIGNED_BY_CALLER"
            timestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(LocalDateTime.now().toString())
        }

        val reqParam = GetResponseRequest().apply {
            messageTypeSelector = message
            callerInformationSystemSignature = signElement(messageTypeSelector)

            println("request = " + objectToString(this))
        }

        val smev = SMEVMessageExchangeService(schemaUrl, soapServiceQname)
        try {
            val port = smev.getPort(soapEndpointQname, SMEVMessageExchangePortType::class.java)
            port.getResponse(reqParam).apply {
                println("resp = " + objectToString(this))
                responseMessage?.response?.messageMetadata?.messageId?.let { sendAck(it) }
            }
        } catch (e: java.lang.Exception) {
            println("ERROR ${e.javaClass.name}: ${e.message}")
        }
    }

    @Throws(Exception::class)
    fun sendAck(messageId: String, accepted: Boolean = true) {
        val message = AckTargetMessage().apply {
            id = "SIGNED_BY_CALLER"
            isAccepted = accepted
            value = messageId
        }

        val reqParam = AckRequest().apply {
            ackTargetMessage = message
            callerInformationSystemSignature = signElement(ackTargetMessage)

            println("request = " + objectToString(this))
        }

        val smev = SMEVMessageExchangeService(schemaUrl, soapServiceQname)
        try {
            val port = smev.getPort(soapEndpointQname, SMEVMessageExchangePortType::class.java)
            port.ack(reqParam)
        } catch (e: java.lang.Exception) {
            println("ERROR ${e.javaClass.name}: ${e.message}")
        }
    }

    private fun signElement(data: Any): XMLDSigSignatureType {
        return XMLDSigSignatureType().apply { any = sign.sign(objectToDocument(data).documentElement) }
    }

    private fun buildMessage(data: String): Element {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        return factory.newDocumentBuilder().parse(InputSource(StringReader(data))).documentElement
    }

    @Throws(JAXBException::class, IOException::class)
    private fun objectToString(obj: Any): String {
        val jaxbMarshaller = JAXBContext.newInstance(obj::class.java).createMarshaller()
        if (prettyPrint)
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        val baos = ByteArrayOutputStream()
        jaxbMarshaller.marshal(obj, baos)
        val content = baos.toString()
        baos.close()
        return content
    }

    @Throws(JAXBException::class, IOException::class)
    fun objectToDocument(obj: Any): Document {
        return XMLTransformHelper.buildDocumentFromString(objectToString(obj))
    }
}