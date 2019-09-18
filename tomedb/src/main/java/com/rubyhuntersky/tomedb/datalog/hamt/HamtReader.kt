package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.datalog.framing.FrameReader

class HamtReader(private val frameReader: FrameReader, private val rootBase: Long?) {

    private sealed class Search {
        data class Continue(val table: HamtTable) : Search()
        object Failure : Search()
        data class Success(val value: Long) : Search()
    }

    fun keys(): Sequence<Long> {
        return rootBase?.let {
            sequence {
                yieldAll(keys(HamtTable.fromRootBytes(frameReader.read(it))))
            }
        } ?: emptySequence()
    }

    fun values(): Sequence<Long> {
        return rootBase?.let {
            sequence {
                yieldAll(values(HamtTable.fromRootBytes(frameReader.read(it))))
            }
        } ?: emptySequence()
    }

    private fun keys(table: HamtTable): Sequence<Long> {
        return sequence {
            table.slots.forEach {
                when (it) {
                    HamtTable.Slot.Empty -> Unit
                    is HamtTable.Slot.KeyValue -> yield(it.key)
                    is HamtTable.Slot.MapBase -> {
                        val subTable = it.toSubTable(frameReader)
                        yieldAll(keys(subTable))
                    }
                }
            }
        }
    }

    private fun values(table: HamtTable): Sequence<Long> {
        return sequence {
            table.slots.forEach {
                when (it) {
                    HamtTable.Slot.Empty -> Unit
                    is HamtTable.Slot.KeyValue -> yield(it.value)
                    is HamtTable.Slot.MapBase -> {
                        val subTable = it.toSubTable(frameReader)
                        yieldAll(keys(subTable))
                    }
                }
            }
        }
    }

    operator fun get(key: Long): Long? {
        return rootBase?.let {
            val rootTable = HamtTable.fromRootBytes(frameReader.read(it))
            val search = Hamt.toIndices(key).fold(
                initial = Search.Continue(rootTable) as Search,
                operation = { search, index ->
                    when (search) {
                        is Search.Continue -> {
                            when (val slot = search.table.getSlot(index)) {
                                is HamtTable.Slot.MapBase -> {
                                    Search.Continue(
                                        slot.toSubTable(frameReader)
                                    )
                                }
                                is HamtTable.Slot.KeyValue -> {
                                    if (slot.key == key) {
                                        Search.Success(
                                            slot.value
                                        )
                                    } else {
                                        Search.Failure
                                    }
                                }
                                is HamtTable.Slot.Empty -> {
                                    Search.Failure
                                }
                            }
                        }
                        is Search.Failure -> search
                        is Search.Success -> search
                    }
                }
            )
            val success = search as? Search.Success
            success?.value
        }
    }
}