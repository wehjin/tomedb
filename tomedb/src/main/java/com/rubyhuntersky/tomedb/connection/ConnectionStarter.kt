package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Ledger

sealed class ConnectionStarter {
    data class Attributes(val attributes: List<Attribute>) : ConnectionStarter()
    data class Data(val reader: Ledger.Reader) : ConnectionStarter()
}