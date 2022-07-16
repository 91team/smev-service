package com.nineone.smev.models

import kotlinx.serialization.*

@Serializable
data class ExchangeResponse(val status: String, val payload: String)