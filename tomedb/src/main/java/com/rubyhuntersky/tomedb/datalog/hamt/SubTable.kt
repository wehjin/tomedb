package com.rubyhuntersky.tomedb.datalog.hamt


sealed class Slot2 {

    object Empty : Slot2()
    data class KeyValue(val key: Long, val value: Long) : Slot2()
    data class SoftLink(val subTable: SubTable) : Slot2()
}

sealed class SubTable(val depth: Int) {

    fun getValue(key: Long): Long? = getValue(Hamt, key)
    fun postValue(key: Long, value: Long): SubTable = postValue(Hamt, key, value)

    abstract fun getValue(keyBreaker: KeyBreaker, key: Long): Long?
    abstract fun postValue(keyBreaker: KeyBreaker, key: Long, value: Long): SubTable

    companion object {

        fun new(): SubTable = emptySubTable(0)
        fun emptySubTable(depth: Int): SubTable = Soft(depth, emptySlots)

        private val emptySlots = arrayListOf<Slot2>()
            .apply { addAll((0 until slotCount).map { Slot2.Empty }) }

        fun isKey(candidate: Long) = !isMap(candidate)
        private fun isMap(candidate: Long) = (candidate and slotMapBit) == slotMapBit
        private const val slotBytes = Long.SIZE_BYTES * 2
        private const val slotMapBit = 1L shl (Long.SIZE_BITS - 1)
        const val slotCount = 32
    }
}

class Hard(depth: Int, base: Long, slotMap: SlotMap) : SubTable(depth) {
    override fun getValue(keyBreaker: KeyBreaker, key: Long): Long? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun postValue(keyBreaker: KeyBreaker, key: Long, value: Long): SubTable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class Soft(depth: Int, private val slots: List<Slot2>) : SubTable(depth) {

    override fun getValue(keyBreaker: KeyBreaker, key: Long): Long? {
        require(isKey(key))
        val index = keyBreaker.slotIndex(key, depth)
        return when (val slot = slots[index]) {
            Slot2.Empty -> {
                null
            }
            is Slot2.KeyValue -> {
                when (key) {
                    slot.key -> slot.value
                    else -> null
                }
            }
            is Slot2.SoftLink -> {
                slot.subTable.getValue(keyBreaker, key)
            }
        }
    }

    override fun postValue(keyBreaker: KeyBreaker, key: Long, value: Long): SubTable {
        require(isKey(key))
        val index = keyBreaker.slotIndex(key, depth)
        return when (val slot = slots[index]) {
            Slot2.Empty -> Soft(depth, slots.replaceSlot(index, Slot2.KeyValue(key, value)))
            is Slot2.KeyValue -> {
                if (key == slot.key) {
                    if (value == slot.value) {
                        this@Soft
                    } else {
                        Soft(depth, slots.replaceSlot(index, Slot2.KeyValue(key, value)))
                    }
                } else {
                    val keyValues = mapOf(slot.key to slot.value, key to value)
                    val subTable = keyValues.entries.fold(
                        initial = emptySubTable(depth + 1),
                        operation = { subTable, (key, value) ->
                            subTable.postValue(keyBreaker, key, value)
                        }
                    )
                    Soft(depth, slots.replaceSlot(index, Slot2.SoftLink(subTable)))
                }
            }
            is Slot2.SoftLink -> {
                val subTable = slot.subTable.postValue(keyBreaker, key, value)
                if (slot.subTable == subTable) {
                    this
                } else {
                    Soft(depth, slots.replaceSlot(index, Slot2.SoftLink(subTable)))
                }
            }
        }
    }

    private fun List<Slot2>.replaceSlot(index: Int, slot: Slot2): List<Slot2> {
        return toMutableList().also { it[index] = slot }
    }
}
