package com.nineone.smev.models

import kotlinx.serialization.*

@Serializable
data class ExchangeGetResponse(val id: String, val senderId: String)