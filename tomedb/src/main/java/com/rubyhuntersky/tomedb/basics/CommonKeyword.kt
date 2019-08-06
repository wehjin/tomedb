package com.rubyhuntersky.tomedb.basics

data class CommonKeyword(
    override val keywordName: String,
    override val keywordGroup: String
) : Keyword {
    constructor(keyword: Keyword) : this(keyword.keywordName, keyword.keywordGroup)

    override fun equals(other: Any?): Boolean = this.keywordEquals(other)
    override fun hashCode(): Int = this.keywordHashCode()
    override fun toString(): String = this.toKeywordString()
}