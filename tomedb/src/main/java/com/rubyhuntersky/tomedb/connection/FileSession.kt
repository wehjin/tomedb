package com.rubyhuntersky.tomedb.connection

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.attributes.attrName
import com.rubyhuntersky.tomedb.attributes.toSchemeData
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.FileTransactor
import com.rubyhuntersky.tomedb.database.entityExistsWithAttrValue
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.scopes.session.Session
import java.io.File


class FileSession(dataDir: File, spec: List<Attribute<*>>?) : Session {

    val transactor = FileTransactor(dataDir)

    init {
        spec?.let {
            val data = it.toNewAttributes().map(Attribute<*>::toSchemeData)
            transactData(data)
        }
    }

    override fun close() = Unit

    private fun List<Attribute<*>>.toNewAttributes(): List<Attribute<*>> = mapNotNull { attribute ->
        val nameValue = (attribute.attrName)
        if (transactor.getDb().entityExistsWithAttrValue(Scheme.NAME.attrName, nameValue)) {
            attribute
        } else {
            println("SKIPPED: Existing attribute: ${attribute.attrName}")
            null
        }
    }

    override fun getDb(): Database = transactor.getDb()

    override fun transactDb(updates: Set<Update>) {
        val expanded = updates.flatMap(this::expandDataValues)
        transactor.update(expanded)
    }

    fun transactData(tagLists: List<TagList>): List<Long> {
        val updates = tagLists.flatMap {
            expandTagList(it, transactor.nextEnt(), Update.Action.Declare)
        }
        return transactor.update(updates).map(Fact::entity).distinctBy { it }
    }

    private fun expandTagList(tagList: TagList, entity: Long, action: Update.Action): List<Update> {
        return tagList.flatMap { (value, attr) ->
            expandDataValues(Update(entity, attr, value, action))
        }
    }

    private fun expandDataValues(update: Update): List<Update> {
        val (entity, attr, value, type) = update
        return if (value is TagList) {
            val subEntity = transactor.nextEnt()
            expandTagList(value, subEntity, type) + Update(entity, attr, subEntity, type)
        } else {
            listOf(update)
        }
    }

    fun commit() = transactor.commit()
}