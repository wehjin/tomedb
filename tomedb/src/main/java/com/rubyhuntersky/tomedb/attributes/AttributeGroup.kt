package com.rubyhuntersky.tomedb.attributes

import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Ident

interface AttributeGroup

fun AttributeGroup.toEnt(index: Long, label: String? = null): Ent {
    return Ident.of(this, index, label).toEnt()
}

val AttributeGroup.groupName: String
    get() = this::class.java.simpleName.let {
        if (it == "Companion") this::class.java.enclosingClass.simpleName else it
    }
