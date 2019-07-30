package com.rubyhuntersky.tomedb.basics

interface NamedItem {

    val itemName: ItemName
        get() = (this as? Enum<*>)
            ?.let { ItemName(this::class.java.simpleName, this.name) }
            ?: ItemName(
                this::class.java.declaringClass?.simpleName ?: "",
                this::class.java.simpleName
            )

    interface Group
}