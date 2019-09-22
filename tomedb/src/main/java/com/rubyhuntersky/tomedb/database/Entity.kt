package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword

data class Entity<KeyT : Any>(
    private val keyAttr: Attribute<*>,
    val key: KeyT,
    private val otherValues: Map<Keyword, Any>
) {
    val ent: Ent by lazy {
        Ent.of(keyAttr.toKeyword(), key)
    }
    val data: Map<Keyword, Any> by lazy {
        otherValues + (keyAttr.toKeyword() to key)
    }

    inline operator fun <reified T : Any> invoke(attr: Attribute<*>): T? {
        return data[attr.toKeyword()] as? T
    }

    fun toUpdates(): List<Update> {
        return data.map { (attr, value) ->
            Update(ent.long, attr, value)
        }
    }

    fun setValue(attr: Attribute<*>, value: Any): Entity<KeyT> {
        require(attr != keyAttr)
        return copy(otherValues = otherValues.toMutableMap().also {
            it[attr.toKeyword()] = value
        })
    }

    fun canReplace(otherEntity: Entity<*>?): Boolean {
        return otherEntity?.let {
            ent == otherEntity.ent && keyAttr == otherEntity.keyAttr && key == otherEntity.key
        } ?: true
    }

    operator fun minus(oldEntity: Entity<KeyT>): List<Update> {
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
        fun <KeyT : Any> from(
            keyAttr: Attribute<*>,
            key: KeyT,
            data: Map<Keyword, Any>
        ): Entity<KeyT> = Entity(keyAttr, key, data)
    }
}
