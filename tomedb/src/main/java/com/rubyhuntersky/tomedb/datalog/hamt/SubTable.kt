package com.rubyhuntersky.tomedb.datalog.hamt

sealed class SubTable {

    abstract fun getValue(indices: Sequence<Byte>, key: Long): Long?
    abstract fun postValue(indices: Sequence<Byte>, key: Long, value: Long): SubTable

    class Hard(base: Long, slotMap: SlotMap) : SubTable() {
        override fun getValue(indices: Sequence<Byte>, key: Long): Long? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun postValue(indices: Sequence<Byte>, key: Long, value: Long): SubTable {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    class Soft(private val slots: List<HamtTable.Slot>) : SubTable() {

        override fun getValue(indices: Sequence<Byte>, key: Long): Long? {
            require(isKey(key))
            val index = indices.first().toInt()
            return when (val slot = slots[index]) {
                HamtTable.Slot.Empty -> null
                is HamtTable.Slot.MapBase -> TODO()
                is HamtTable.Slot.KeyValue -> slot.value
            }
        }

        override fun postValue(indices: Sequence<Byte>, key: Long, value: Long): SubTable {
            require(isKey(key))
            val index = indices.first().toInt()
            return when (val slot = slots[index]) {
                HamtTable.Slot.Empty -> {
                    Soft(slots.toMutableList().also {
                        it[index] = HamtTable.Slot.KeyValue(key, value)
                    })
                }
                is HamtTable.Slot.MapBase -> TODO()
                is HamtTable.Slot.KeyValue -> TODO()
            }
        }
    }

    companion object {

        fun new(): SubTable = Soft(emptySlots)

        private val emptySlots = arrayListOf<HamtTable.Slot>()
            .apply { addAll((0 until slotCount).map { HamtTable.Slot.Empty }) }

        private fun isKey(candidate: Long) = !isMap(candidate)
        private fun isMap(candidate: Long) = (candidate and mapBit) == mapBit
        private const val slotBytes = Long.SIZE_BYTES * 2
        private const val mapBit = 1L shl (Long.SIZE_BITS - 1)
        private const val slotCount = 32
    }
}