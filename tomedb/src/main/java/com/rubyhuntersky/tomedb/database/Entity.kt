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

data class Entity(
    private val keyAttr: Attribute,
    val key: Date,
    private val otherValues: Map<Keyword, Any>
) {
    val ent: Ent by lazy {
        Ent.of(keyAttr.toKeyword(), key)
    }
    val data: Map<Keyword, Any> by lazy {
        otherValues + (keyAttr.toKeyword() to key)
    }
    val page: Page<Date> by lazy {
        val subject = PageSubject.TraitHolder(
            traitHolder = ent,
            traitValue = key,
            topic = TomeTopic.Trait(keyAttr)
        )
        pageOf(subject, data)
    }

    inline operator fun <reified T : Any> invoke(attr: Attribute): T? {
        return data[attr.toKeyword()] as? T
    }

    fun toUpdates(): List<Update> {
        return data.map { (attr, value) ->
            Update(ent.long, attr, value)
        }
    }

    fun setValue(attr: Attribute, value: Any): Entity {
        require(attr != keyAttr)
        return copy(otherValues = otherValues.toMutableMap().also {
            it[attr.toKeyword()] = value
        })
    }

    fun canReplace(otherEntity: Entity?): Boolean {
        return otherEntity?.let {
            ent == otherEntity.ent && keyAttr == otherEntity.keyAttr && key == otherEntity.key
        } ?: true
    }

    operator fun minus(oldEntity: Entity): List<Update> {
        require(this.ent == oldEntity.ent)
        val ent = this.ent.long
        val lateData = this.data
        val earlyData = oldEntity.data
        val (addAttrs, modAttrs, dropAttrs) = toAddModDrop(
            lateAttrs = lateData.keys,
            earlyAttrs = earlyData.keys
        )
        val dropUpdates = dropAttrs.mapNotNull { attr ->
            earlyData[attr]?.let { earlyValue ->
                Update(ent, attr, earlyValue, Update.Action.Retract)
            }
        }
        val modUpdates = modAttrs.mapNotNull { attr ->
            lateData[attr]?.let { lateValue ->
                if (lateValue != earlyData[attr]) {
                    Update(ent, attr, lateValue, Update.Action.Declare)
                } else {
                    null
                }
            }
        }
        val addUpdates = addAttrs.mapNotNull { attr ->
            lateData[attr]?.let { lateValue ->
                Update(ent, attr, lateValue, Update.Action.Declare)
            }
        }
        return dropUpdates + modUpdates + addUpdates
    }

    private fun toAddModDrop(
        lateAttrs: Set<Keyword>,
        earlyAttrs: Set<Keyword>
    ): Triple<Set<Keyword>, Set<Keyword>, Set<Keyword>> {
        val add = lateAttrs - earlyAttrs
        val mod = lateAttrs - add
        val drop = earlyAttrs - mod
        return Triple(add, mod, drop)
    }

    companion object {
        fun from(keyAttr: Attribute, key: Date, data: Map<Keyword, Any>): Entity {
            return Entity(keyAttr, key, data)
        }
    }
}
