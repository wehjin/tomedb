package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.MutableDatabase
import com.rubyhuntersky.tomedb.datalog.Fact
import java.io.File


class FileSession(dataDir: File, spec: List<Attribute>?) {

    val mutDb = MutableDatabase(dataDir)

    init {
        spec?.let {
            val data = it.toNewAttributes().map(Attribute::toSchemeData)
            transactData(data)
        }
    }

    private fun List<Attribute>.toNewAttributes(): List<Attribute> = mapNotNull { attribute ->
        val nameValue = (attribute.attrName)
        if (mutDb.entityExistsWithAttrValue(Scheme.NAME.attrName, nameValue)) {
            attribute
        } else {
            println("SKIPPED: Existing attribute: ${attribute.attrName}")
            null
        }
    }

    fun send(update: Update) = send(setOf(update))
    fun send(updates: Set<Update>) {
        val expanded = updates.flatMap(this::expandDataValues)
        mutDb.update(expanded)
    }

    fun checkout(): Database {
        return mutDb
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
        return if (value is TagList) {
            val subEntity = mutDb.nextEntity()
            expandTagList(value, subEntity, type) + Update(entity, attr, subEntity, type)
        } else {
            listOf(update)
        }
    }

    @Deprecated(message = "Use send.")
    fun update(entity: Long, attr: Keyword, value: Any, isAsserted: Boolean = true) {
        val type = Update.Action.valueOf(isAsserted)
        val update = Update(entity, attr, value, type)
        send(setOf(update))
    }

    fun commit() = mutDb.commit()
}