package com.rubyhuntersky.tomedb.basics

import java.util.*

fun Value?.asString(): String = (this as Value.STRING).v
fun Value?.asLong(): Long = (this as Value.LONG).v

val b64Encoder = Base64.getEncoder()
val b64Decoder = Base64.getDecoder()
