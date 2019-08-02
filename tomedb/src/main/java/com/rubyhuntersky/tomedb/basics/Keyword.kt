package com.rubyhuntersky.tomedb.basics

interface Keyword {
    val keywordName: String
        get() = (this as? Enum<*>)?.let { this.name } ?: this::class.java.simpleName

    val keywordGroup: String
        get() = (this as? Enum<*>)
            ?.let {
                this::class.java.simpleName.let {
                    if (it != name) it else this::class.java.enclosingClass?.simpleName ?: ""
                }
            }
            ?: this::class.java.declaringClass?.simpleName ?: ""

    fun toKeywordString(): String = "$keywordGroup/$keywordName"

    fun keywordEquals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Keyword) return false

        if (keywordName != other.keywordName) return false
        if (keywordGroup != other.keywordGroup) return false

        return true
    }

    fun keywordHashCode(): Int {
        var result = keywordName.hashCode()
        result = 31 * result + keywordGroup.hashCode()
        return result
    }

}