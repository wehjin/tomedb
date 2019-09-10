package com.rubyhuntersky.tomedb.datalog.hamt

import kotlin.math.absoluteValue

object UniHash {

    fun hashLong(l: Long) = hashBytes(Hamt.bytesFromLong(l))

    private fun hashBytes(key: ByteArray): Long {
        val (hash, _) = key.fold(
            initial = Pair(0L, 31415L),
            operation = { (hash, a), keyByte -> Pair(a * hash + keyByte.toLong(), a * 27183L) }
        )
        return hash.absoluteValue
    }
}