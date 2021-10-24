

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import ru.voskhod.crypto.XMLTransformHelper

import ru.voskhod.smev.client.api.identification.impl.IdentificationServiceImpl
import ru.voskhod.smev.client.api.services.identification.IdentityService
import ru.voskhod.smev.client.api.services.signature.Signer
import ru.voskhod.smev.client.api.signature.configuration.SignatureConfigurationImpl
import ru.voskhod.smev.client.api.signature.impl.SignerImpl

import ru.it.smev.message_exchange.autogenerated.types.basic.v1_3.*
import ru.it.smev.message_exchange.autogenerated.types.v1_3.SenderProvidedRequestData
import ru.it.smev.message_exchange.autogenerated.types.v1_3.SendRequestRequest
import ru.it.smev.message_exchange.autogenerated.types.basic.v1_3.Void

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.util.ArrayList
import java.util.List
import java.security.KeyStore


fun main(args: Array<String>) {
    // Initialize JCP and keystore
    val ks = KeyStore.getInstance("HDImageStore", "JCP")
    ks.load(null, null)
    println("Available aliases: ${ks.aliases().toList()}")
    Smev.run()
}