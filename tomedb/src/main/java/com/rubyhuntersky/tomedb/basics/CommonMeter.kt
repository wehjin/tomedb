package com.rubyhuntersky.tomedb.basics

data class CommonMeter(override val meterName: String, override val meterGroup: String) :
    Meter {

    override fun equals(other: Any?): Boolean = this.meterEquals(other)
    override fun hashCode(): Int = this.meterHashCode()
    override fun toString(): String = this.toMeterString()
}