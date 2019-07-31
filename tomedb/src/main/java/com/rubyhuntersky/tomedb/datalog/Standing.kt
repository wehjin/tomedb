package com.rubyhuntersky.tomedb.datalog

sealed class Standing {
    companion object {
        fun valueOf(isAsserted: Boolean): Standing {
            return if (isAsserted) Asserted else Retracted
        }
    }

    object Asserted : Standing()
    object Retracted : Standing()
}