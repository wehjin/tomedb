package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.MutableDatabase
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.invoke
import com.rubyhuntersky.tomedb.datalog.Fact
import java.nio.file.Path

class Connection(dataPath: Path, spec: List<Attribute>?) {

    val database = MutableDatabase(dataPath)

    init {
        spec?.let { transactData(it.map(Attribute::tagList)) }
    }

    fun transactData(tagLists: List<TagList>): List<Long> {
        val updates = tagLists.flatMap {
            expandTagList(it, database.nextEntity(), Update.Type.Assert)
        }
        return database.update(updates).map(Fact::entity).distinctBy { it }
    }

    private fun expandTagList(tagList: TagList, entity: Long, type: Update.Type): List<Update> {
        return tagList.flatMap { (value, attr) ->
            expandDataValues(Update(entity, attr, value, type))
        }
    }

    private fun expandDataValues(update: Update): List<Update> {
        val (entity, attr, value, type) = update
        return if (value is Value.DATA) {
            val tagList = value.v
            val subEntity = database.nextEntity()
            expandTagList(tagList, subEntity, type) + Update(entity, attr, subEntity(), type)
        } else {
            listOf(update)
        }
    }

    fun update(entity: Long, attr: Keyword, value: Value<*>, isAsserted: Boolean = true) {
        val type = Update.Type.valueOf(isAsserted)
        val updates = expandDataValues(Update(entity, attr, value, type))
        database.update(updates)
    }

    fun commit() = database.commit()
}