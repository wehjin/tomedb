package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.data.*
import java.util.*

class Entity(val page: Page<Date>, private val keyAttr: Attribute) {

    val key: Date
        get() = this(keyAttr)!!

    inline operator fun <reified T : Any> invoke(attr: Attribute): T? {
        return page[attr] as? T
    }

    companion object {
        fun from(page: Page<Date>, keyAttr: Attribute): Entity {
            return Entity(page, keyAttr)
        }

        fun from(tome: Tome<Date>, key: Date, data: Map<Keyword, Any>): Entity {
            val keyAttr = (tome.topic as TomeTopic.Trait<Date>).attr
            val page = pageOf(tome.newPageSubject(key), data)
            return Entity(page, keyAttr)
        }
    }
}