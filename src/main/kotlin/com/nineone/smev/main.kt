package com.nineone.smev

import java.security.KeyStore
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    System.setProperty("logging.level", System.getenv("LOG_LEVEL") ?: "ERROR")

    when (args.firstOrNull()) {
        "aliases" -> {
            // Initialize JCP and keystore
            val ks = KeyStore.getInstance("HDImageStore", "JCP")
            ks.load(null, null)
            println("Available key aliases: ${ks.aliases().toList()}")
        }
        "server" -> {
            println("Starting SMEV service")
            Service(initClient()).run()
        }
        "ack" -> {
            var uid = "de16f324-012f-11ed-bc46-52540000001e"
            println("Starting SMEV service")
            println("Send ACK")
            var client = initClient()
            client.sendAck(uid)
        }
        else -> {
            println("Command not found")
            exitProcess(1)
        }
    }
}

fun initClient(): Client {
    // Initialize SMEV service
     return Client(
         schemaUrl = System.getenv("SCHEMA_URL"),
         keyAlias =  System.getenv("KEY_ALIAS"),
         keyPassword = System.getenv("KEY_PASSWORD"),
         nodeId = System.getenv("NODE_ID"),
         soapServiceName = System.getenv("SOAP_SERVICE_NAME"),
         soapEndpointName = System.getenv("SOAP_ENDPOINT_NAME"),
         isTest = System.getenv("TEST_MESSAGE") == "true",
         prettyPrint = true
    )
}