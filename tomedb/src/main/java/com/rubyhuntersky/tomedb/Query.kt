package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

sealed class Query {
    data class Find(
        val inputs: List<Input>? = null,
        val rules: List<Rule>,
        val outputs: List<String>
    ) : Query()

    class Find2(init: Find2.() -> Unit) : Query() {
        internal var rules: List<Rule2> = emptyList()

        init {
            this.init()
        }

        sealed class Rule2 {
            data class SlotAttrValue(val eSlot: Slot, val attr: Keyword, val value: Value<*>) : Rule2()
            data class SlotAttrSlot(val eSlot: Slot, val attr: Keyword, val vSlot: Slot) : Rule2()
            data class SlotAttrESlot(val eSlot: Slot, val attr: Keyword, val eSlot2: ESlot) : Rule2()
            data class SlipValue(val slip: Slip, val value: Value<*>) : Rule2()
            data class Slide(val keywordNames: List<String>) : Rule2() {
                infix fun and(keywordName: String): Slide = Slide(keywordNames + keywordName)
            }
        }

        data class SlotAttr(val slot: Slot, val attr: Keyword) {
            infix fun eq(value: Value<*>): Rule2.SlotAttrValue = Rule2.SlotAttrValue(this.slot, this.attr, value)
            infix fun eq(slotName: String): Rule2.SlotAttrSlot =
                Rule2.SlotAttrSlot(this.slot, this.attr, CommonSlot(slotName))

            infix fun eq(eSlot: ESlot): Rule2.SlotAttrESlot = Rule2.SlotAttrESlot(this.slot, this.attr, eSlot)
        }

        data class Slip(val name: String) {
            infix fun put(value: Value<*>): Rule2.SlipValue = Rule2.SlipValue(this, value)
        }

        interface Slot : Keyword {
            operator fun invoke(results: List<Map<String, Value<*>>>): List<Value<*>> =
                results.mapNotNull { it[keywordName] }

            infix fun capture(attr: Keyword): SlotAttr = SlotAttr(this, attr)
            operator fun unaryMinus() = Rule2.Slide(listOf(this.keywordName))
        }

        data class CommonSlot(override val keywordName: String) : Slot
        data class ESlot(override val keywordName: String) : Slot

        infix fun String.capture(attr: Keyword): SlotAttr = CommonSlot(this).capture(attr)
        operator fun String.unaryPlus() = Slip(this)
        operator fun String.unaryMinus() = Rule2.Slide(listOf(this))
        operator fun String.not() = ESlot(this)

    }
}