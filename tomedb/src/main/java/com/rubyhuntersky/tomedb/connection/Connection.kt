package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.MutableDatabase
import com.rubyhuntersky.tomedb.Query
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.invoke
import com.rubyhuntersky.tomedb.datalog.Fact
import java.io.File


interface Database {
    fun find2(query: Query.Find2): List<Map<String, Value<*>>>
}

class Connection(dataDir: File, spec: List<Attribute>?) {

    val mutDb = MutableDatabase(dataDir)

    init {
        spec?.let { transactData(it.map(Attribute::tagList)) }
    }

    fun send(updates: Set<Update>) {
        val expanded = updates.flatMap(this::expandDataValues)
        mutDb.update(expanded)
    }

    fun checkout(): Database {
        TODO()
    }

    fun transactData(tagLists: List<TagList>): List<Long> {
        val updates = tagLists.flatMap {
            expandTagList(it, mutDb.nextEntity(), Update.Action.Declare)
        }
        return mutDb.update(updates).map(Fact::entity).distinctBy { it }
    }

    private fun expandTagList(tagList: TagList, entity: Long, action: Update.Action): List<Update> {
        return tagList.flatMap { (value, attr) ->
            expandDataValues(Update(entity, attr, value, action))
        }
    }

    private fun expandDataValues(update: Update): List<Update> {
        val (entity, attr, value, type) = update
        return if (value is Value.DATA) {
            val tagList = value.v
            val subEntity = mutDb.nextEntity()
            expandTagList(tagList, subEntity, type) + Update(entity, attr, subEntity(), type)
        } else {
            listOf(update)
        }
    }

    @Deprecated(message = "Use send.")
    fun update(entity: Long, attr: Keyword, value: Value<*>, isAsserted: Boolean = true) {
        val type = Update.Action.valueOf(isAsserted)
        val update = Update(entity, attr, value, type)
        send(setOf(update))
    }

    fun commit() = mutDb.commit()
}