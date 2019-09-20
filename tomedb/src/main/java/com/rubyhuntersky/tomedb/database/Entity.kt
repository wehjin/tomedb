package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.data.Page
import com.rubyhuntersky.tomedb.data.PageSubject
import com.rubyhuntersky.tomedb.data.TomeTopic
import com.rubyhuntersky.tomedb.data.pageOf
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

        fun from(keyAttr: Attribute, key: Date, data: Map<Keyword, Any>): Entity {
            val subject = PageSubject.TraitHolder(
                traitHolder = Ent.of(keyAttr, key),
                traitValue = key,
                topic = TomeTopic.Trait(keyAttr)
            )
            val page = pageOf(subject, data + (keyAttr.toKeyword() to key))
            return Entity(page, keyAttr)
        }
    }
}