package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.database.Database
import java.io.File

interface Tomic {
    fun getDb(): Database
    fun write(mods: List<Mod<*>>)
    fun close()
}

inline fun <reified T : Any> Tomic.ownerList(property: Attribute2<T>): List<Owner<T>> {
    return visitOwnersOf(property) { owners.values.toList() }
}

inline fun <reified T : Any, R> Tomic.visitOwnersOf(
    property: Attribute2<T>,
    noinline block: OwnerHive<T>.() -> R
): R = ownersOf(property).visit(block)

inline fun <reified T : Any> Tomic.ownersOf(property: Attribute2<T>): OwnerHive<T> {
    val basis = getDb()
    return object : OwnerHive<T> {
        override val basis: Database = basis
        override val owners: Map<Long, Owner<T>> by lazy {
            getOwners(basis, property).associateBy(Owner<T>::ent)
        }
        override val any: Owner<T>? by lazy {
            when (owners.any()) {
                true -> owners.values.first()
                false -> null
            }
        }
        override val ownerList: List<Owner<T>> by lazy { owners.values.toList() }

        override fun Map<Long, Owner<T>>.matchKey(key: T): Owner<T>? {
            return owners.values.firstOrNull { key == it[property] }
        }
    }
}

inline fun <reified T : Any, R> Tomic.modOwnersOf(
    property: Attribute2<T>,
    noinline block: ModOwnerHive<T>.() -> R
): R {
    var hive = ownersOf(property)
    val modHive = object : ModOwnerHive<T> {
        override val basis: Database get() = hive.basis
        override val owners: Map<Long, Owner<T>> get() = hive.owners
        override val any: Owner<T>? get() = hive.any
        override val ownerList: List<Owner<T>> get() = hive.ownerList
        override fun Map<Long, Owner<T>>.matchKey(key: T): Owner<T>? =
            hive.run { this.owners.matchKey(key) }

        override var mods: List<Mod<*>> = emptyList()
            set(value) {
                check(field.isEmpty())
                field = value.also { write(value) }
                hive = ownersOf(property)
            }

    }
    return modHive.run(block)
}

fun tomicOf(dir: File, init: TomicScope.() -> List<Attribute<*>>): Tomic {
    val spec = object : TomicScope {}.init()
    val session = startSession(dir, spec)
    return object : Tomic {
        override fun close() = session.close()
        override fun getDb(): Database = session.getDb()

        override fun write(mods: List<Mod<*>>) {
            val updates = mods.map {
                val updateType = when (it) {
                    is Mod.Set -> UpdateType.Declare
                    is Mod.Clear -> UpdateType.Retract
                }
                Update(it.ent, it.attribute.toKeyword(), it.quantAsScript(), updateType)
            }
            session.transactDb(updates.toSet())
        }
    }
}

interface TomicScope
