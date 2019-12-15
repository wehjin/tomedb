package com.rubyhuntersky.tomedb.app

import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.data.startSession
import com.rubyhuntersky.tomedb.database.Database
import java.io.File

interface Tomic<E : Any> {
    fun close()
    fun readLatest(): Database
    fun <E1 : E> write(edit: E1)
}

fun <E : Any> tomicOf(dir: File, init: TomicScope<E>.() -> List<Attribute<*>>): Tomic<E> {
    val handlers = mutableSetOf<EditHandler<*>>()
    val spec = object : TomicScope<E> {
        override fun <E1 : E> on(editClass: Class<E1>, handler: EditScope<E1>.() -> Unit) {
            handlers.add(EditHandler(editClass, handler))
        }
    }.init()
    val session = startSession(dir, spec)
    val handlerMap = handlers.associateBy { it.editClass }
    return object : Tomic<E> {
        override fun close() = session.close()
        override fun readLatest(): Database = session.getDb()
        override fun <E1 : E> write(edit: E1) {
            val handler = handlerMap[edit::class.java]
            handler?.handle(edit, ::transact)
        }

        private fun transact(attr: Attribute<*>, value: Any) {
            val update = Update(0, attr.toKeyword(), value)
            session.transactDb(setOf(update))
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
    val write: (attr: Attribute<*>, value: Any) -> Unit
}

data class EditHandler<E1 : Any>(
    val editClass: Class<E1>,
    val handler: EditScope<E1>.() -> Unit
) {
    fun handle(anyEdit: Any, transact: (attr: Attribute<*>, value: Any) -> Unit) {
        val typedEdit = editClass.cast(anyEdit)!!
        object : EditScope<E1> {
            override val edit: E1 = typedEdit
            override val write = transact
        }.apply(handler)
    }
}
