package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker

sealed class Query {
    data class Find(
        val inputs: List<Input<*>>? = null,
        val rules: List<Rule>,
        val outputs: List<String>
    ) : Query()

    @ScopeTagMarker
    class Find2(init: Find2.() -> Unit) : Query() {

        lateinit var rules: List<Rule2>

        init {
            this.init()
        }

        sealed class Rule2 {
            data class SlotAttrValue(val eSlot: Slot, val attr: Keyword, val value: Value<*>) : Rule2()
            data class SlotAttrSlot(val eSlot: Slot, val attr: Keyword, val vSlot: Slot) : Rule2()
            data class SlotAttrESlot(val eSlot: Slot, val attr: Keyword, val eSlot2: ESlot) : Rule2()
            data class SlotAttr(val slot: Slot, val attr: Keyword) : Rule2() {
                infix fun eq(value: Value<*>): SlotAttrValue = SlotAttrValue(this.slot, this.attr, value)
                infix fun eq(slotName: String): SlotAttrSlot = eq(CommonSlot(slotName))
                infix fun eq(slot: Slot): SlotAttrSlot = SlotAttrSlot(this.slot, this.attr, slot)
                infix fun eq(eSlot: ESlot): SlotAttrESlot = SlotAttrESlot(this.slot, this.attr, eSlot)
            }

            data class SlipValue(val slip: Slip, val value: Value<*>) : Rule2()
            data class Slide(val keywordNames: List<String>) : Rule2() {
                infix fun and(keywordName: String): Slide = Slide(keywordNames + keywordName)
                infix fun and(slot: Slot): Slide = Slide(keywordNames + slot.keywordName)
            }
        }


        data class Slip(val name: String) {
            infix fun put(value: Value<*>): Rule2.SlipValue = Rule2.SlipValue(this, value)
        }

        interface Slot : Keyword {
            operator fun invoke(results: List<Map<String, Value<*>>>): List<Value<*>> =
                results.mapNotNull { it[keywordName] }

            operator fun invoke(rows: FindResult): List<Value<*>> = rows(this)

            infix fun capture(attr: Keyword): Rule2.SlotAttr = Rule2.SlotAttr(this, attr)
            operator fun unaryMinus() = Rule2.Slide(listOf(this.keywordName))
        }

        data class CommonSlot(override val keywordName: String) : Slot {
            override fun toString(): String = "Slot/$keywordName"
        }

        data class ESlot(override val keywordName: String) : Slot {
            override fun toString(): String = "ESlot/$keywordName"
        }

        infix fun String.has(attr: Keyword): Rule2.SlotAttr = CommonSlot(this).capture(attr)
        operator fun String.unaryPlus() = Slip(this)
        operator fun String.unaryMinus() = Rule2.Slide(listOf(this))
        operator fun String.not() = ESlot(this)

    }
}