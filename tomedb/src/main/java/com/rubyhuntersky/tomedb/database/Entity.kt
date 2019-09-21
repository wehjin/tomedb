package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.Update
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
    private val otherValues: Map<Keyword, Any>
) {
    val ent: Ent by lazy {
        Ent.of(keyAttr, key)
    }
    val values: Map<Keyword, Any> by lazy {
        otherValues + (keyAttr.toKeyword() to key)
    }
    val page: Page<Date> by lazy {
        val subject = PageSubject.TraitHolder(
            traitHolder = ent,
            traitValue = key,
            topic = TomeTopic.Trait(keyAttr)
        )
        pageOf(subject, values)
    }

    inline operator fun <reified T : Any> invoke(attr: Attribute): T? {
        return values[attr.toKeyword()] as? T
    }

    fun toUpdates(): List<Update> {
        return values.map { (attr, value) ->
            Update(ent.long, attr, value)
        }
    }

    companion object {
        fun from(keyAttr: Attribute, key: Date, data: Map<Keyword, Any>): Entity {
            return Entity(keyAttr, key, data)
        }
    }
}
