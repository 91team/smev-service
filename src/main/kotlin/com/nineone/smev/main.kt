package com.nineone.smev

import java.security.KeyStore
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when (args[0]) {
        "list-aliases" -> {
            // Initialize JCP and keystore
            val ks = KeyStore.getInstance("HDImageStore", "JCP")
            ks.load(null, null)
            println("Available aliases: ${ks.aliases().toList()}")
        }
        "send-request" -> {
            initSmev().sendRequest()
        }
        "get-response" -> {
            initSmev().getResponse()
        }
        "send-ack" -> {
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

    //    return SMEVService(
    //        schemaUrl = "http://172.20.3.12:5000/ws/smev-message-exchange-service-1.3.wsdl",
    //        keyAlias = "1026402203068 1006170807 - Copy",
    //        keyPassword = "12345678"
    //    )
     return SMEVService(
        schemaUrl = "http://smev3-n0.test.gosuslugi.ru:5000/ws/smev-message-exchange-service-1.3.wsdl",
        keyAlias = "skspb",
        keyPassword = "1234567890",
        isTest = true
    )
}