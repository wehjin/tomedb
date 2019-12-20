package com.rubyhuntersky.tomedb

interface EntHolder {
    val ent: Long
}

fun EntHolder.reform(
    init: EntReformScope.() -> Unit
): List<Form<*>> = reformEnt(ent, init)
