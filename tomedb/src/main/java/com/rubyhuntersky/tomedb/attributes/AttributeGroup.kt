package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Ident

interface AttributeGroup {
    val groupName: String
        get() = this::class.java.simpleName.let {
            if (it == "Companion") this::class.java.enclosingClass.simpleName else it
        }

    fun toEnt(index: Long, label: String? = null): Ent = Ident.of(this, index, label).toEnt()
}