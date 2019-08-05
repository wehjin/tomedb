package com.rubyhuntersky.tomedb.attributes

interface AttributeGroup {
    val groupName: String
        get() = this::class.java.simpleName.let {
            if (it == "Companion") this::class.java.enclosingClass.simpleName else it
        }
}