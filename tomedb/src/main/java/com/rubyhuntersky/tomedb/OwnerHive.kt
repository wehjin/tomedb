package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.database.Database

interface OwnerHive<T : Any> {
    val basis: Database
    val owners: Map<Long, Owner<T>>
    val any: Owner<T>?
    val ownerList: List<Owner<T>>
}

fun <T : Any, R> OwnerHive<T>.visit(block: OwnerHive<T>.() -> R): R = run(block)

interface ModOwnerHive<T : Any> : OwnerHive<T> {
    var mods: List<Mod<*>>
}

fun <T : Any, R> ModOwnerHive<T>.visit(block: ModOwnerHive<T>.() -> R) = this.run(block)
