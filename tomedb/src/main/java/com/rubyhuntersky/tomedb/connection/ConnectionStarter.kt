package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.MeterSpec

sealed class ConnectionStarter {
    data class MeterSpecs(val meters: List<MeterSpec>) : ConnectionStarter()
    object None : ConnectionStarter()
}