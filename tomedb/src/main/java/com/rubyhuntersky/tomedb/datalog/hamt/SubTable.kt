package com.rubyhuntersky.tomedb.datalog.hamt

sealed class SubTable(val depth: Int) {

    fun getValue(key: Long) = getValue(Hamt, key)
    fun getValue(keyBreaker: KeyBreaker, key: Long): Long? {
        require(isKey(key))
        val index = keyBreaker.slotIndex(key, depth)
        return when (val slot = getSlot(index)) {
            is Slot2.Empty -> null
            is Slot2.KeyValue -> if (key == slot.key) slot.value else null
            is Slot2.SoftLink, is Slot2.HardLink -> {
                slot.asSubTable(depth + 1).getValue(keyBreaker, key)
            }
        }
    }

    fun setValues(keyValues: List<Pair<Long, Long>>): SubTable {
        return keyValues.fold(this, { sub, (key, value) ->
            sub.setValue(key, value)
        })
    }

    fun setValue(key: Long, value: Long) = setValue(Hamt, key, value)
    fun setValue(keyBreaker: KeyBreaker, key: Long, value: Long): SubTable {
        require(isKey(key))
        val index = keyBreaker.slotIndex(key, depth)
        return when (val slot = getSlot(index)) {
            Slot2.Empty -> setSlot(index, Slot2.KeyValue(key, value))
            is Slot2.KeyValue -> {
                if (key == slot.key) {
                    if (value == slot.value) {
                        this@SubTable
                    } else {
                        setSlot(index, Slot2.KeyValue(key, value))
                    }
                } else {
                    val keyValues = mapOf(slot.key to slot.value, key to value)
                    val subTable = keyValues.entries.fold(
                        initial = empty(depth + 1) as SubTable,
                        operation = { subTable, (key, value) ->
                            subTable.setValue(keyBreaker, key, value)
                        }
                    ) as Soft
                    setSlot(index, subTable.asSoftLink())
                }
            }
            is Slot2.SoftLink, is Slot2.HardLink -> {
                val oldSub = slot.asSubTable(depth + 1)
                when (val newSub = oldSub.setValue(keyBreaker, key, value)) {
                    is Soft -> setSlot(index, newSub.asSoftLink())
                    is Hard -> this
                }
            }
        }
    }

    abstract fun getSlot(index: Int): Slot2
    abstract fun setSlot(index: Int, slot: Slot2): Soft
    abstract fun harden(io: SubTableIo): Hard

    class Soft(depth: Int, private val slots: ArrayList<Slot2>) : SubTable(depth) {

        override fun getSlot(index: Int): Slot2 = slots[index]

        override fun setSlot(index: Int, slot: Slot2) = Soft(
            depth = depth,
            slots = ArrayList<Slot2>(slots.size).also {
                it.addAll(slots)
                it[index] = slot
            }
        )

        override fun harden(io: SubTableIo): Hard {
            val hardSlots = slots.map {
                when (it) {
                    Slot2.Empty -> it
                    is Slot2.KeyValue -> it
                    is Slot2.HardLink -> it
                    is Slot2.SoftLink -> it.subTable.harden(io).asHardLink()
                }
            }
            val (slotMap, base) = io.writeSlots(hardSlots)
            return Hard(depth, slotMap, base, io.reader)
        }

        fun asSoftLink() = Slot2.SoftLink(this)
    }

    class Hard(
        depth: Int,
        val slotMap: SlotMap,
        val base: Long,
        private val reader: SubTableReader
    ) : SubTable(depth) {

        override fun getSlot(index: Int): Slot2 {
            return slotMap.getOffsetToSlot(index)?.let { reader.readSlot(base + it) } ?: Slot2.Empty
        }

        override fun setSlot(index: Int, slot: Slot2): Soft {
            return Soft(depth, reader.readSlots(slotMap, base)).setSlot(index, slot)
        }

        override fun harden(io: SubTableIo): Hard = this

        fun asHardLink() = Slot2.HardLink(slotMap, base, reader)
    }

    companion object {

        fun new(): SubTable = empty(0)

        fun load(
            slotMap: SlotMap,
            base: Long,
            reader: SubTableReader
        ): SubTable = Hard(0, slotMap, base, reader)

        fun empty(depth: Int) = Soft(depth, emptySlots())

        fun isKey(candidate: Long) = !SlotMap.isMap(candidate)
        fun emptySlots() =
            arrayListOf<Slot2>().apply { addAll((0 until slotCount).map { Slot2.Empty }) }

        const val slotCount = 32

    }
}

