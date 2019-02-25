package com.rubyhuntersky.tomedb

class Ref {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ref) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}