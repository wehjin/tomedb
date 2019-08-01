package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.AttrSpec

sealed class ConnectionStarter {
    data class AttrSpecs(val attrs: List<AttrSpec>) : ConnectionStarter()
    object None : ConnectionStarter()
}