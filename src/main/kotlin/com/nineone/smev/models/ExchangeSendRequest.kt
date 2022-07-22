package com.nineone.smev.models

import kotlinx.serialization.*

@Serializable
data class ExchangeSendRequest(val id: String, val payload: String)