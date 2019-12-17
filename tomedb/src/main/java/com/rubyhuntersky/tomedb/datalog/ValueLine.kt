package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.basics.longFromBytes
import java.util.*

data class ValueLine(
    val value: Any,
    val standing: Standing,
    val instant: Date,
    val height: TxnId
) {
    fun flip(instant: Date, height: TxnId): ValueLine {
        return copy(standing = standing.flip(), instant = instant, height = height)
    }

    fun toBytes(): ByteArray {
        val valueBytes = value.toFolderName().toByteArray()
        val standingByte = standing.asByte()
        val instantBytes =
            bytesFromLong(instant.time)
        val heightBytes = height.toBytes()
        return valueBytes + standingByte + instantBytes + heightBytes
    }

    companion object {
        fun from(fact: Fact): ValueLine =
            ValueLine(
                fact.value,
                fact.standing,
                fact.inst,
                fact.txn
            )

        fun from(byteArray: ByteArray): ValueLine {
            val valueLen = byteArray.size - 1 - Long.SIZE_BYTES - TxnId.bytesLen
            val instantStart = valueLen + 1
            val heightStart = instantStart + TxnId.bytesLen
            val valueBytes = byteArray.sliceArray(0 until valueLen)
            val standingByte = byteArray[valueLen]
            val instantBytes = byteArray.sliceArray(instantStart until heightStart)
            val heightBytes = byteArray.sliceArray(heightStart until byteArray.size)
            val value = valueOfFolderName(
                String(valueBytes)
            )
            return ValueLine(
                value = value,
                standing = Standing.from(
                    standingByte
                ),
                instant = Date(
                    longFromBytes(
                        instantBytes
                    )
                ),
                height = TxnId.from(
                    heightBytes
                )
            )
        }
    }
}