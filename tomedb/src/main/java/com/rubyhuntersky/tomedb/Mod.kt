package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity
import com.rubyhuntersky.tomedb.database.entitiesWith

sealed class Mod<T> {
    data class Set<T : Any>(
        val ent: Long,
        val attribute: Attribute2<T>,
        val value: T
    ) : Mod<T>() {
        fun asScription(): String = attribute.scriber.scribe(value)
    }
}

fun entMods(ent: Long, init: EntModScope.() -> Unit): List<Mod<*>> {
    return mutableListOf<Mod<*>>().also { mods ->
        object : EntModScope {
            override fun <T : Any> bind(attr: Attribute2<T>, value: T) {
                mods.add(Mod.Set(ent, attr, value))
            }
        }.init()
    }
}

interface EntModScope {
    fun <T : Any> bind(attr: Attribute2<T>, value: T)
}

interface WrappingAttribute<S : Any, W> : Attribute<S> {
    fun wrap(source: S): W?
    fun unwrap(wrapped: W): S
}


interface Owner<S : Any> {
    operator fun <W> get(attribute: WrappingAttribute<S, W>): W?
}

inline fun <reified T : Any> Database.getOwners(propertyAttribute: Attribute<T>): List<Owner<T>> {
    val entities = entitiesWith(propertyAttribute).toList()
    return entities.map<Entity<T>, Owner<T>> { entity ->
        object : Owner<T> {
            override fun <W> get(attribute: WrappingAttribute<T, W>): W? {
                return projectValue(
                    entity,
                    attribute
                )
            }
        }
    }
}

inline fun <reified S : Any, T> projectValue(
    entity: Entity<S>,
    attribute: WrappingAttribute<S, T>
): T? = entity(attribute)?.let { attribute.wrap(it) }
