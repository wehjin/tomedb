package com.rubyhuntersky.tomedb

interface EntHolder {
    val ent: Long
}

fun EntHolder.mod(
    init: EntModScope.() -> Unit
): List<Mod<*>> = modEnt(ent, init)
