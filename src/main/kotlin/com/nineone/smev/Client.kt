package com.nineone.smev


import org.apache.xml.security.utils.resolver.ResourceResolver
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import ru.it.smev.message_exchange.autogenerated.service.v1_2.SMEVMessageExchangePortType
import ru.it.smev.message_exchange.autogenerated.service.v1_2.SMEVMessageExchangeService
import ru.it.smev.message_exchange.autogenerated.types.basic.v1_2.*
import ru.it.smev.message_exchange.autogenerated.types.v1_2.*
import ru.voskhod.crypto.XMLTransformHelper
import ru.voskhod.smev.client.api.identification.impl.IdentificationServiceImpl
import ru.voskhod.smev.client.api.services.signature.Signer
import ru.voskhod.smev.client.api.services.template.configuration.MTOMAttachmentTemplateConfiguration
import ru.voskhod.smev.client.api.signature.configuration.SignatureConfigurationImpl
import ru.voskhod.smev.client.api.signature.impl.SignerImpl
import ru.voskhod.smev.client.api.template.impl.MTOMAttachmentTemplateImpl
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.net.URL
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.datatype.DatatypeFactory
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory


class Client(schemaUrl: String?, // точка доступа СМЭВ 3
             keyAlias: String,
             keyPassword: String,
             private val isTest: Boolean = false,
             soapServiceName: String?,
             soapEndpointName: String?,
             private val nodeId: String? = null,
             private val prettyPrint: Boolean = false
) {
    private val schemaUrl = URL(schemaUrl ?: "http://172.20.3.12:5000/ws/smev-message-exchange-service-1.3.wsdl")
    private val smevNamespaceUri: String = "urn://x-artefacts-smev-gov-ru/services/message-exchange/1.2"
    private val soapServiceQname: QName = QName(smevNamespaceUri, soapServiceName ?: "SMEVMessageExchangeService")
    private val soapEndpointQname: QName = QName(smevNamespaceUri, soapEndpointName ?: "SMEVMessageExchangeEndpoint")
    private var sign: Signer
    private val logger = LoggerFactory.getLogger(this.javaClass)

    init {
        val signConfig = SignatureConfigurationImpl("JCP2", keyAlias, keyAlias, keyPassword, null)
        sign = SignerImpl(signConfig)
        sign.init(keyAlias, keyAlias, keyPassword)
//        JCPXMLDSigInit.init()
        ResourceResolver.registerAtStart("ru.CryptoPro.JCPxml.utility.DocumentIdResolver")
    }

    fun sendRequest(message: String) {
        sendRequest(buildMessage(message), IdentificationServiceImpl().generateUUID())
    }

    @Throws(Exception::class)
    fun sendRequest(message: Element, messageId: String, files: MutableList<File> = ArrayList()): SendRequestResponse {
        val sendReq = SenderProvidedRequestData().apply {
            id = "SIGNED_BY_CALLER"
            messageID = messageId
            nodeID = nodeId
            testMessage = Void().takeIf { isTest }
            messagePrimaryContent = MessagePrimaryContent().apply { any = message }
            attachmentHeaderList = setMTOMFile(sign, files).takeIf { files.isNotEmpty() }
        }

        val reqParam = SendRequestRequest().apply {
            senderProvidedRequestData = sendReq
            callerInformationSystemSignature = signElement(senderProvidedRequestData)
            attachmentContentList = setAttContLst(files).takeIf { files.isNotEmpty() }

            logger.debug("SendRequestRequest(REQUEST): {}", objectToString(this))
            File("logs/${messageId}-SendRequestRequest-REQUEST.xml").writeText(objectToString(this))
        }


        val smev = SMEVMessageExchangeService(schemaUrl, soapServiceQname)
        val port = smev.getPort(soapEndpointQname, SMEVMessageExchangePortType::class.java)
        return port.sendRequest(reqParam).apply {
            logger.debug("SendRequestRequest(RESPONSE): {}", objectToString(this))
            File("logs/${messageId}-SendRequestRequest-RESPONSE.xml").writeText(objectToString(this))
        }
    }

    @Throws(Exception::class)
    fun getResponse(targetMessageId: String?, senderId: String?): GetResponseResponse {
        val dateTime = OffsetDateTime.now(ZoneId.of("Europe/Moscow"))

        val message = MessageTypeSelector().apply {
            id = "SIGNED_BY_CALLER"
            timestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateTime.toString())
            nodeID = nodeId
            senderIdentifier = senderId.takeIf { senderId !== null } // "30cd0d"
            messageID = targetMessageId.takeIf { targetMessageId !== null }
//            senderRole = "2"
//            originatorId = "36539c"
//            namespaceURI = "urn://x-artefacts-rosreestr-gov-ru/virtual-services/egrn-statement/1.1.2"
//            rootElementLocalName = "Response"
        }

        val reqParam = GetResponseRequest().apply {
            messageTypeSelector = message
            callerInformationSystemSignature = signElement(messageTypeSelector)
            logger.debug("GetResponseRequest(REQUEST): {}", objectToString(this))
//            File("logs/${targetMessageId}-GetResponseRequest-REQUEST.xml").writeText(objectToString(this))
        }

        val smev = SMEVMessageExchangeService(schemaUrl, soapServiceQname)
        val port = smev.getPort(soapEndpointQname, SMEVMessageExchangePortType::class.java)
        return port.getResponse(reqParam).apply {
            logger.info("GetResponseRequest(RESPONSE): {}", objectToString(this))
            responseMessage?.response?.messageMetadata?.messageId?.let {
                File("logs/${responseMessage?.response?.originalMessageId}-GetResponseRequest-RESPONSE.xml").writeText(objectToString(this))
                sendAck(it)
            }
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
            logger.info("AckRequest(REQUEST): {}", objectToString(this))
            File("logs/${messageId}-AckRequest-REQUEST.xml").writeText(objectToString(this))
        }

        val smev = SMEVMessageExchangeService(schemaUrl, soapServiceQname)
        try {
            val port = smev.getPort(soapEndpointQname, SMEVMessageExchangePortType::class.java)
            port.ack(reqParam)
        } catch (e: java.lang.Exception) {
            logger.error("ERROR sendAck", e)
        }
    }

    private fun signElement(data: Any): XMLDSigSignatureType {
        return XMLDSigSignatureType().apply { any = sign.sign(objectToDocument(data).documentElement) }
    }

    private fun setMTOMFile(signer: Signer, lstFiles: List<File>): AttachmentHeaderList {
        val headLst = AttachmentHeaderList()
        val mtomTempl = MTOMAttachmentTemplateImpl<MTOMAttachmentTemplateConfiguration>(signer, IdentificationServiceImpl())

        val lstMtom = mtomTempl.createAttachments(lstFiles)
        for (i in lstMtom.indices) {
            val attHdTp = AttachmentHeaderType()
            attHdTp.contentId = lstMtom[i].attachmentId // имя файла для запроса
            attHdTp.mimeType = lstMtom[i].mimeType // тип файла для запроса
            attHdTp.signaturePKCS7 = lstMtom[i].signaturePKCS7
            headLst.attachmentHeader.add(attHdTp)
        }
        return headLst
    }

    private fun setAttContLst(pathFile: List<File>): AttachmentContentList {
        val contentLst = AttachmentContentList()
        for (i in pathFile.indices) {
            val attContTp = AttachmentContentType()
            val dtHandl = DataHandler(FileDataSource(pathFile[i])) // указываем путь до файла
            attContTp.content = dtHandl
            attContTp.id = dtHandl.name // имя файла для запроса
            contentLst.attachmentContent.add(attContTp)
        }
        return contentLst
    }

    private fun buildMessage(data: String): Element {
        return DocumentBuilderFactory
            .newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(InputSource(StringReader(data)))
            .documentElement
    }

    @Throws(JAXBException::class, IOException::class)
    private fun objectToString(obj: Any): String {
        val baos = ByteArrayOutputStream()
        val jaxbMarshaller = JAXBContext.newInstance(obj::class.java).createMarshaller()
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint)
        jaxbMarshaller.marshal(obj, baos)
        return baos.toString().also { baos.close() }
    }

    @Throws(JAXBException::class, IOException::class)
    fun objectToDocument(obj: Any): Document = XMLTransformHelper.buildDocumentFromString(objectToString(obj))
}