package com.rubyhuntersky.tomedb.basics

fun Value?.asString(): String = (this as Value.STRING).v
fun Value?.asLong(): Long = (this as Value.LONG).v
