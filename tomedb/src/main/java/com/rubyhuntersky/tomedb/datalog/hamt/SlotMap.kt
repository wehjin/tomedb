package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.basics.bytesFromLong

data class SlotMap(val bits: Long) {
    init {
        require(isMap(bits))
    }

    fun setIndex(index: Int) = copy(bits = bits or 1L.shl(index))

    fun toBytes() = bytesFromLong(bits and flag)

    fun getOffsetToSlot(index: Int): Int? {
        return if (isSlotPresent(index)) {
            val indent = (0..index).fold(
                initial = 0,
                operation = { count, next -> if (isSlotPresent(next)) count + 1 else count }
            )
            indent * SubTableReadWrite.slotBytes
        } else null
    }

    fun isSlotPresent(index: Int) = (1L.shl(index) and bits) != 0L

    companion object {
        fun empty() = SlotMap(flag)
        fun isMap(candidate: Long) = (candidate and flag) == flag
        private const val flag = 1L shl (Long.SIZE_BITS - 1)
    }
}
