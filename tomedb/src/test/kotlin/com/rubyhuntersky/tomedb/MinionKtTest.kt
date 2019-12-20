package com.rubyhuntersky.tomedb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.absoluteValue
import kotlin.random.Random

class MinionKtTest : TomeTest("minionTest") {

    @Test
    fun crud() {
        val tome = startTome("crud")
        val leader = Leader(1000, Wallet.Owner)
        val create = tome.reformMinions(leader) {
            val ent = Random.nextLong().absoluteValue
            reforms = formMinion(ent) { Wallet.Dollars set Amount(1) }
            minion(ent)
        }
        assertEquals(leader, create.leader)

        val read = tome.visitMinions(leader) {
            val minion = minion(create.ent)
            checkNotNull(minion[Wallet.Dollars])
        }
        assertEquals(Amount(1), read)

        val update = tome.reformMinions(leader) {
            reforms = minion(create.ent).reform { Wallet.Dollars set read + Amount(4) }
            minion(create.ent)[Wallet.Dollars]
        }
        assertEquals(Amount(5), update)

        val delete = tome.reformMinions(leader) {
            val minion = minion(create.ent)
            val minionUnform = minion.unform
            reforms = minionUnform
            val after = minionOrNull(create.ent)
            after
        }
        assertNull(delete)
    }
}
