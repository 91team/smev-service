package com.nineone.smev

import java.io.File
import java.security.KeyStore
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when (args.firstOrNull()) {
        "aliases" -> {
            // Initialize JCP and keystore
            val ks = KeyStore.getInstance("HDImageStore", "JCP")
            ks.load(null, null)
            println("Available key aliases: ${ks.aliases().toList()}")
        }
        "request" -> {
            initSmev().sendRequest(File(args[1]).readText())
        }
        "response" -> {
            initSmev().getResponse()
        }
        "ack" -> {
            initSmev().sendAck(args[1])
        }
        else -> {
            print("Command not found")
            exitProcess(1)
        }
    }
}

fun initSmev(): SMEVService {
    // Initialize SMEV service
     return SMEVService(
         schemaUrl = System.getenv("SCHEMA_URL"),
         keyAlias =  System.getenv("KEY_ALIAS"),
         keyPassword = System.getenv("KEY_PASSWORD"),
         soapServiceName = System.getenv("SOAP_SERVICE_NAME"),
         soapEndpointName = System.getenv("SOAP_ENDPOINT_NAME"),
         isTest = System.getenv("TEST_MESSAGE") == "true",
         prettyPrint = false
    )
}