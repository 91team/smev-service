package com.nineone.smev

import java.security.KeyStore
import kotlin.system.exitProcess

import io.ktor.server.engine.*
import io.ktor.server.netty.*

import com.nineone.smev.plugins.*

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
        "webserver" -> {
            println("Starting SMEV web service")
//            Service(initClient()).run()
            embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
                configureRouting()
                configureSerialization()
            }.start(wait = true)
        }
        "ack" -> {
            var uid = "436b595a-01cf-11ed-bc46-52540000001e"
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