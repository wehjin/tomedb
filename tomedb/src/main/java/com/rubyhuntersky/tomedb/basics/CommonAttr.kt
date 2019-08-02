package com.rubyhuntersky.tomedb.basics

data class CommonAttr(override val attrName: String, override val attrGroup: String) :
    Attr {

    override fun equals(other: Any?): Boolean = this.attrEquals(other)
    override fun hashCode(): Int = this.attrHashCode()
    override fun toString(): String = this.toAttrString()
}