package com.rubyhuntersky.tomedb.basics

interface NamedItem {

    val itemName: ItemName
        get() = (this as? Enum<*>)
            ?.let {
                val elementName = this.name
                val className = this::class.java.simpleName
                val groupName = if (className != elementName) {
                    className
                } else {
                    this::class.java.enclosingClass?.simpleName ?: ""
                }
                ItemName(groupName, elementName)
            }
            ?: ItemName(
                this::class.java.declaringClass?.simpleName ?: "",
                this::class.java.simpleName
            )

    interface Group
}