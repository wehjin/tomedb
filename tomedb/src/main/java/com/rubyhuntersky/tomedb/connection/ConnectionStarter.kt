package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Attribute

sealed class ConnectionStarter {
    data class Attributes(val attributes: List<Attribute>) : ConnectionStarter()
    object None : ConnectionStarter()
}