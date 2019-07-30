package com.rubyhuntersky.tomedb.basics

import java.util.*

interface TimeClock {
    val now: Date

    object REALTIME : TimeClock {
        override val now: Date
            get() = Date()
    }
}