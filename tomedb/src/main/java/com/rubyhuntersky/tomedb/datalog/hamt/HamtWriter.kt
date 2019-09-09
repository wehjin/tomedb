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
        data class Descend(
            val table: HamtTable,
            val ancestors: List<Pair<HamtTable, Byte>>
        ) : Insert()

        data class DescendConflicted(
            val conflict: Conflict,
            val ancestors: List<Pair<HamtTable?, Byte>>
        ) : Insert()

        data class Ascend(
            val revisedTable: HamtTable,
            val rewriteTables: List<Pair<HamtTable?, Byte>>
        ) : Insert()
    }

    fun put(key: Long, value: Long) {
        rootBase = rootBase?.let {
            val rootTable = HamtTable(
                frameReader.read(it),
                HamtTableType.Root
            )
            when (val insert = descend(rootTable, key, value)) {
                is Insert.Descend -> error("Subtable found at final index from key: $key")
                is Insert.DescendConflicted -> error("Hash collision for keys: $key, ${insert.conflict.key}")
                is Insert.Ascend -> {
                    val topTable = insert.rewriteTables.fold(
                        initial = insert.revisedTable,
                        operation = { subTable, (supTable, index) ->
                            TODO()
                        }
                    )
                    TODO()
                }
            }
            TODO()
        } ?: createRootTable(key, value)
    }

    private fun descend(rootTable: HamtTable, key: Long, value: Long): Insert {
        return HamtKey(key).toIndices().fold(
            initial = Insert.Descend(rootTable, emptyList()) as Insert,
            operation = { insert, index ->
                when (insert) {
                    is Insert.Descend -> {
                        when (val slotContent = insert.table.getSlotContent(index)) {
                            is HamtTable.SlotContent.MapBase -> {
                                val subTable = slotContent.toSubTable(frameReader)
                                Insert.Descend(
                                    table = subTable,
                                    ancestors = insert.ancestors + Pair(insert.table, index)
                                )
                            }
                            is HamtTable.SlotContent.Empty -> {
                                val revisedTable = insert.table.fillSlot(index, key, value)
                                Insert.Ascend(
                                    revisedTable,
                                    insert.ancestors.reversed()
                                )
                            }
                            is HamtTable.SlotContent.KeyValue -> {
                                if (slotContent.key == key) {
                                    val revisedTable = insert.table.fillSlot(index, key, value)
                                    Insert.Ascend(
                                        revisedTable,
                                        insert.ancestors.reversed()
                                    )
                                } else {
                                    val extendedAncestors =
                                        insert.ancestors + Pair(insert.table, index)
                                    val conflictIndices = HamtKey(
                                        slotContent.key
                                    ).toIndices()
                                        .drop(extendedAncestors.size)
                                    val conflict =
                                        Conflict(
                                            slotContent.key,
                                            slotContent.value,
                                            conflictIndices
                                        )
                                    Insert.DescendConflicted(
                                        conflict = conflict,
                                        ancestors = extendedAncestors
                                    )
                                }
                            }
                        }
                    }
                    is Insert.DescendConflicted -> {
                        val conflictIndex = insert.conflict.indices.first()
                        if (conflictIndex == index) {
                            Insert.DescendConflicted(
                                conflict = insert.conflict,
                                ancestors = insert.ancestors + Pair(null, index)
                            )
                        } else {
                            val revisedTable =
                                HamtTable.createSub(
                                    setOf(
                                        Triple(index, key, value),
                                        Triple(
                                            conflictIndex,
                                            insert.conflict.key,
                                            insert.conflict.value
                                        )
                                    )
                                )
                            Insert.Ascend(
                                revisedTable,
                                insert.ancestors.reversed()
                            )
                        }
                    }
                    is Insert.Ascend -> insert
                }
            }
        )
    }

    private fun createRootTable(key: Long, value: Long): Long {
        val index = HamtKey(key).toIndices().first()
        val rootTable =
            HamtTable.createRoot(index, key, value)
        val bytes = rootTable.toBytes()
        return frameWriter.write(bytes)
    }
}