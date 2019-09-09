package com.rubyhuntersky.tomedb.datalog.hamt

sealed class HamtTableType {
    object Root : HamtTableType()
    data class Sub(val map: Long) : HamtTableType()
}