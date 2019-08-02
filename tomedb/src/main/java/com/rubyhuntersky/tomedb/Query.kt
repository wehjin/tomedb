package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Attr
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

        infix fun String.capture(attr: Attr): SlotAttr = SlotAttr(Slot(this), attr)
        operator fun String.unaryPlus() = Slip(this)
        operator fun String.unaryMinus() = Rule2.Slide(listOf(this))
        operator fun String.not() = ESlot(this)

        sealed class Rule2 {
            data class SlotAttrValue(val eSlot: Slot, val attr: Attr, val value: Value<*>) : Rule2()
            data class SlotAttrSlot(val eSlot: Slot, val attr: Attr, val vSlot: Slot) : Rule2()
            data class SlotAttrESlot(val eSlot: Slot, val attr: Attr, val eSlot2: ESlot) : Rule2()
            data class SlipValue(val slip: Slip, val value: Value<*>) : Rule2()
            data class Slide(val names: List<String>) : Rule2() {
                infix fun and(name: String): Slide = Slide(names + name)
            }
        }

        data class SlotAttr(val slot: Slot, val attr: Attr) {
            infix fun eq(value: Value<*>): Rule2.SlotAttrValue = Rule2.SlotAttrValue(this.slot, this.attr, value)
            infix fun eq(slotName: String): Rule2.SlotAttrSlot =
                Rule2.SlotAttrSlot(this.slot, this.attr, Slot(slotName))

            infix fun eq(eSlot: ESlot): Rule2.SlotAttrESlot = Rule2.SlotAttrESlot(this.slot, this.attr, eSlot)
        }

        data class Slot(val name: String)
        data class Slip(val name: String) {
            infix fun put(value: Value<*>): Rule2.SlipValue = Rule2.SlipValue(this, value)
        }

        data class ESlot(val name: String)
    }
}