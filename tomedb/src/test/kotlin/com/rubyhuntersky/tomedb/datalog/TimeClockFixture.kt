package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.TimeClock
import java.util.*

internal class TimeClockFixture : TimeClock {

    var nextNow: Date = Date()

    override val now: Date
        get() = nextNow
}