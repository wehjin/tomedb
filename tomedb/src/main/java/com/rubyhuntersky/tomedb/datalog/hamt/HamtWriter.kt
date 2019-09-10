package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import java.io.InputStream

class HamtWriter(
    inputStream: InputStream,
    private var rootBase: Long?,
    private val frameWriter: FrameWriter
) {

    private val frameReader = FrameReader(inputStream)

    private data class Conflict(
        val key: Long,
        val value: Long,
        val indices: Sequence<Byte>
    )

    private sealed class Insert {
        data class Revised(
            val revisedTable: HamtTable,
            val upperTables: List<Pair<HamtTable?, Byte>>
        ) : Insert()

        data class Descending(
            val table: HamtTable,
            val upperTables: List<Pair<HamtTable, Byte>>
        ) : Insert()

        data class DescendConflicted(
            val conflict: Conflict,
            val upperTables: List<Pair<HamtTable?, Byte>>
        ) : Insert()
    }

    fun put(key: Long, value: Long) {
        val nextRoot = rootBase?.let {
            val rootTable = HamtTable(frameReader.read(it), HamtTableType.Root)
            when (val insert = descend(rootTable, key, value)) {
                is Insert.Descending -> error("Sub-table found in slot at lowest sub-table of key: $key")
                is Insert.DescendConflicted -> error("Key hashes collided: $key, ${insert.conflict.key}")
                is Insert.Revised -> {
                    insert.upperTables.reversed().fold(
                        initial = insert.revisedTable,
                        operation = { revised, (original, index) ->
                            val revisedBase = frameWriter.write(revised.toBytes())
                            original?.fillSlotWithMapBase(index, revised.map, revisedBase)
                                ?: HamtTable.createSubWithMapBase(
                                    index = index,
                                    map = revised.map,
                                    base = revisedBase
                                )
                        }
                    )
                }
            }
        } ?: HamtTable.createRoot(Hamt.toIndices(key).first(), key, value)
        rootBase = frameWriter.write(nextRoot.toRootBytes())
    }

    private fun descend(rootTable: HamtTable, key: Long, value: Long): Insert {
        return Hamt.toIndices(key).fold(
            initial = Insert.Descending(rootTable, emptyList()) as Insert,
            operation = { insert, index ->
                when (insert) {
                    is Insert.Revised -> insert
                    is Insert.Descending -> {
                        when (val slotContent = insert.table.getSlotContent(index)) {
                            is HamtTable.SlotContent.MapBase -> {
                                val upperTables = insert.upperTables + Pair(insert.table, index)
                                val subTable = slotContent.toSubTable(frameReader)
                                Insert.Descending(subTable, upperTables)
                            }
                            is HamtTable.SlotContent.Empty -> {
                                val revised = insert.table.fillSlotWithKeyValue(index, key, value)
                                Insert.Revised(revised, insert.upperTables)
                            }
                            is HamtTable.SlotContent.KeyValue -> {
                                if (slotContent.key == key) {
                                    val revised =
                                        insert.table.fillSlotWithKeyValue(index, key, value)
                                    Insert.Revised(revised, insert.upperTables)
                                } else {
                                    val upperTables = insert.upperTables + Pair(insert.table, index)
                                    val conflict = Conflict(
                                        slotContent.key,
                                        slotContent.value,
                                        Hamt.toIndices(slotContent.key).drop(upperTables.size)
                                    )
                                    Insert.DescendConflicted(conflict, upperTables)
                                }
                            }
                        }
                    }
                    is Insert.DescendConflicted -> {
                        val conflictIndex = insert.conflict.indices.first()
                        if (conflictIndex == index) {
                            val upperTables = insert.upperTables + Pair(null, index)
                            Insert.DescendConflicted(insert.conflict, upperTables)
                        } else {
                            val revised = HamtTable.createSubWithKeyValues(
                                setOf(
                                    Triple(index, key, value),
                                    Triple(
                                        conflictIndex,
                                        insert.conflict.key,
                                        insert.conflict.value
                                    )
                                )
                            )
                            Insert.Revised(revised, insert.upperTables)
                        }
                    }
                }
            }
        )
    }
}