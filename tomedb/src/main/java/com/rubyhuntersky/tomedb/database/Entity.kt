package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.data.Page
import com.rubyhuntersky.tomedb.data.PageSubject
import com.rubyhuntersky.tomedb.data.TomeTopic
import com.rubyhuntersky.tomedb.data.pageOf
import java.util.*

class Entity(
    private val keyAttr: Attribute,
    val key: Date,
    val values: Map<Keyword, Any>
) {
    val page: Page<Date> by lazy {
        val subject = PageSubject.TraitHolder(
            traitHolder = Ent.of(keyAttr, key),
            traitValue = key,
            topic = TomeTopic.Trait(keyAttr)
        )
        pageOf(subject, values + (keyAttr.toKeyword() to key))
    }

    inline operator fun <reified T : Any> invoke(attr: Attribute): T? {
        return values[attr.toKeyword()] as? T
    }

    companion object {
        fun from(keyAttr: Attribute, key: Date, data: Map<Keyword, Any>): Entity {
            return Entity(keyAttr, key, data)
        }
    }
}