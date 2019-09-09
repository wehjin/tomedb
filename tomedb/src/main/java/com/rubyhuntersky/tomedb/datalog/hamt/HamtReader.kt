package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import java.io.InputStream

class HamtReader(inputStream: InputStream, private val rootBase: Long?) {

    private val frameReader = FrameReader(inputStream)

    private sealed class Search {
        data class Continue(val table: HamtTable) : Search()
        object Failure : Search()
        data class Success(val value: Long) : Search()
    }

    operator fun get(key: Long): Long? {
        return rootBase?.let {
            val rootTable = HamtTable(
                frameReader.read(it),
                HamtTableType.Root
            )
            val search = HamtKey(key).toIndices().fold(
                initial = Search.Continue(rootTable) as Search,
                operation = { search, index ->
                    when (search) {
                        is Search.Continue -> {
                            when (val slotContent = search.table.getSlotContent(index)) {
                                is HamtTable.SlotContent.MapBase -> {
                                    Search.Continue(
                                        slotContent.toSubTable(frameReader)
                                    )
                                }
                                is HamtTable.SlotContent.KeyValue -> {
                                    if (slotContent.key == key) {
                                        Search.Success(
                                            slotContent.value
                                        )
                                    } else {
                                        Search.Failure
                                    }
                                }
                                is HamtTable.SlotContent.Empty -> {
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