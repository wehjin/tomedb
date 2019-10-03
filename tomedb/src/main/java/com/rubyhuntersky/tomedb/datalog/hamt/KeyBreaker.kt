package com.rubyhuntersky.tomedb.datalog.hamt

interface KeyBreaker {
    fun slotIndex(key: Long, depth: Int): Int
}