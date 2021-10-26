
import java.security.KeyStore

fun main(args: Array<String>) {
    // Initialize JCP and keystore
    val ks = KeyStore.getInstance("HDImageStore", "JCP")
    ks.load(null, null)

    println("Available aliases: ${ks.aliases().toList()}")

    // Initialize SMEV service
//    val smevService = SMEVService(
//        schemaUrl = "http://smev3-n0.test.gosuslugi.ru:5000/ws/smev-message-exchange-service-1.3.wsdl",
//        keyAlias = "skspb",
//        keyPassword = "1234567890",
//        isTest = true
//    )
    val smevService = SMEVService(
//        schemaUrl = "http://smev3-n0.test.gosuslugi.ru:5000/ws/smev-message-exchange-service-1.3.wsdl",
        schemaUrl = "http://172.20.3.12:5000/ws/smev-message-exchange-service-1.3.wsdl",
//        schemaUrl = "http://smev3-d.test.gosuslugi.ru:5000/ws/smev-message-exchange-service-1.3.wsdl",

        keyAlias = "1026402203068 1006170807 - Copy",
        keyPassword = "12345678"
    )

    // Send message to SMEV
    smevService.send()

    // Recieve response from SMEV
//    smevService.receive()

    // Send Ack to SMEV
//    smevService.ack("17b94c4e-35f2-11ec-86b4-acde48001122")
}