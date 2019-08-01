package com.rubyhuntersky.tomedb.basics

interface Attr {
    val attrName: String
        get() = (this as? Enum<*>)?.let { this.name } ?: this::class.java.simpleName

    val attrGroup: String
        get() = (this as? Enum<*>)
            ?.let {
                this::class.java.simpleName.let {
                    if (it != name) it else this::class.java.enclosingClass?.simpleName ?: ""
                }
            }
            ?: this::class.java.declaringClass?.simpleName ?: ""

    fun toAttrString(): String = "$attrGroup/$attrName"

    fun attrEquals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Attr) return false

        if (attrName != other.attrName) return false
        if (attrGroup != other.attrGroup) return false

        return true
    }

    fun attrHashCode(): Int {
        var result = attrName.hashCode()
        result = 31 * result + attrGroup.hashCode()
        return result
    }
}

