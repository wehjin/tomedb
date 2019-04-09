package com.rubyhuntersky.tomedb

sealed class ConnectionStarter {
    data class Attributes(val attributes: List<Attribute>) : ConnectionStarter()
    data class Data(val reader: Ledger.Reader) : ConnectionStarter()
}