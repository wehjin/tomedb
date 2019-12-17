package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.Attribute2
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity
import com.rubyhuntersky.tomedb.scopes.session.Session
import com.rubyhuntersky.tomedb.scopes.session.transact
import java.io.File

fun <E : Any> tomicOf(dir: File, init: TomicScope<E>.() -> List<Attribute<*>>): Tomic<E> {
    val handlers = mutableSetOf<EditHandler<*>>()
    val spec = object : TomicScope<E> {
        override fun <E1 : E> on(editClass: Class<E1>, handler: EditScope<E1>.() -> Unit) {
            handlers.add(
                EditHandler(
                    editClass,
                    handler
                )
            )
        }
    }.init()
    val session = startSession(dir, spec)
    val handlerMap = handlers.associateBy { it.editClass }
    return object : Tomic<E> {
        override fun close() = session.close()
        override fun getDb(): Database = session.getDb()

        override fun <E1 : E> write(edit: E1) {
            val handler = handlerMap[edit::class.java]
            handler?.handle(edit, session)
        }

        override fun write(mods: List<Mod<*>>) {
            val updates = mods.map {
                when (it) {
                    is Mod.Set -> Update(it.ent, it.attribute.toKeyword(), it.asScription())
                }
            }
            session.transactDb(updates.toSet())
        }
    }
}

interface Tomic<E : Any> {
    fun getDb(): Database
    fun <E1 : E> write(edit: E1)
    fun write(mods: List<Mod<*>>)
    fun close()
}

class TomicOwnerHive<T : Any>(
    override val basis: Database,
    override val owners: List<Owner<T>>,
    private val writeMods: (List<Mod<*>>) -> Unit
) : OwnerHive<T> {
    override val first: Owner<T> by lazy { owners.first() }
    override var mods: List<Mod<*>>? = null
        set(value) {
            require(field == null)
            field = value?.also { writeMods(it) }
        }
}

inline fun <reified T : Any> sequenceOwners(
    tome: Tomic<*>,
    property: Attribute2<T>
): Sequence<Owner<T>> = tome.visitOwners(property) { this.owners.asSequence() }

inline fun <reified T : Any, R> Tomic<*>.visitOwners(
    property: Attribute2<T>,
    noinline block: OwnerHive<T>.() -> R
): R {
    var result = Result.failure<R>(Exception("Empty"))
    return collectOwners(property) {
        result = Result.success(this.run(block))
    }.let {
        result.getOrThrow()
    }
}

inline fun <reified T : Any> Tomic<*>.collectOwners(
    property: Attribute2<T>,
    noinline init: OwnerHive<T>.() -> Unit
): OwnerHive<T> {
    val basis = getDb()
    val owners = getOwners(basis, property)
    val hive = TomicOwnerHive(basis, owners, writeMods = { write(it) })
    return hive.apply(init)
}

interface OwnerHive<T : Any> {
    val basis: Database
    val owners: List<Owner<T>>
    val first: Owner<T>
    var mods: List<Mod<*>>?
}

fun <T : Any, R> OwnerHive<T>.visit(block: OwnerHive<T>.() -> R) = this.run(block)

interface TomicScope<E : Any> {
    fun <E1 : E> on(
        editClass: Class<E1>,
        handler: EditScope<E1>.() -> Unit
    )
}

interface EditScope<E1 : Any> {
    val edit: E1
    fun write(attr: Attribute<*>, value: Any)
    fun write(updates: Set<Update>)
    fun <KeyT : Any> EditScope<E1>.write(newEntity: Entity<KeyT>?, oldEntity: Entity<KeyT>?)
}

data class EditHandler<E1 : Any>(
    val editClass: Class<E1>,
    val handler: EditScope<E1>.() -> Unit
) {
    fun handle(anyEdit: Any, session: Session) {
        val typedEdit = editClass.cast(anyEdit)!!

        object : EditScope<E1> {
            override val edit: E1 = typedEdit
            override fun write(attr: Attribute<*>, value: Any) {
                session.transact(attr, value)
            }

            override fun <KeyT : Any> EditScope<E1>.write(
                newEntity: Entity<KeyT>?,
                oldEntity: Entity<KeyT>?
            ) {
                session.transact(newEntity, oldEntity)
            }

            override fun write(updates: Set<Update>) {
                session.transactDb(updates)
            }
        }.apply(handler)
    }
}
