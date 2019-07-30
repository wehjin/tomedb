package com.rubyhuntersky.tomedb.basics

data class NamedItem(val first: String, val last: String) {
    override fun toString(): String = "$first/$last"
}