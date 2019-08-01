package com.rubyhuntersky.tomedb.basics

interface Meter {
    val meterName: String
        get() = (this as? Enum<*>)?.let { this.name } ?: this::class.java.simpleName

    val meterGroup: String
        get() = (this as? Enum<*>)
            ?.let {
                this::class.java.simpleName.let {
                    if (it != name) it else this::class.java.enclosingClass?.simpleName ?: ""
                }
            }
            ?: this::class.java.declaringClass?.simpleName ?: ""

    fun toMeterString(): String = "$meterGroup/$meterName"

    fun meterEquals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Meter) return false

        if (meterName != other.meterName) return false
        if (meterGroup != other.meterGroup) return false

        return true
    }

    fun meterHashCode(): Int {
        var result = meterName.hashCode()
        result = 31 * result + meterGroup.hashCode()
        return result
    }
}

