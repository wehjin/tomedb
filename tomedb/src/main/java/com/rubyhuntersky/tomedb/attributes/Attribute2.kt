package com.rubyhuntersky.tomedb.attributes

interface Attribute2<T> : GroupedItem, Attribute<String> {
    val scriber: Scriber<T>
}

interface Scriber<T> {
    fun scribe(quant: T): String
    fun unscribe(script: String): T
}
