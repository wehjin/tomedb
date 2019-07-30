package com.rubyhuntersky.tomedb.basics

data class ItemName(val first: String, val last: String) {
    override fun toString(): String = "$first/$last"
}