package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.database.Database
import com.rubyhuntersky.tomedb.database.Entity
import com.rubyhuntersky.tomedb.scopes.session.Session
import com.rubyhuntersky.tomedb.scopes.session.transact
import java.io.File

interface Tomic<E : Any> {
    fun readLatest(): Database
    fun <E1 : E> write(edit: E1)
    fun close()
}

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
        override fun readLatest(): Database = session.getDb()
        override fun <E1 : E> write(edit: E1) {
            val handler = handlerMap[edit::class.java]
            handler?.handle(edit, session)
        }
    }
}

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
