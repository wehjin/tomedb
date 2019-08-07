package com.rubyhuntersky.tomedb.scopes.client

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.*
import com.rubyhuntersky.tomedb.connection.Session
import com.rubyhuntersky.tomedb.scopes.ScopeTagMarker
import com.rubyhuntersky.tomedb.scopes.session.ConnectionScope
import com.rubyhuntersky.tomedb.scopes.session.SessionChannel
import com.rubyhuntersky.tomedb.scopes.session.SessionMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.util.*

@ScopeTagMarker
interface ClientScope : CoroutineScope, DestructuringScope {

    val dbDir: File
    val dbSpec: List<Attribute>

    fun connectToDatabase(): ConnectionScope {
        val channel = Channel<SessionMsg>(10)
        val job = launch(Dispatchers.IO) {
            val session = Session(dbDir, dbSpec)
            for (msg in channel) {
                when (msg) {
                    is SessionMsg.UPDATE -> session.send(msg.updates)
                    is SessionMsg.BATCH -> {
                        val ents = session.transactData(msg.tagLists)
                        msg.backChannel.send(ents)
                        msg.backChannel.close()
                    }
                    is SessionMsg.FIND -> {
                        val db = session.checkout()
                        val result = db.find(msg.query)
                        msg.backChannel.send(result)
                        msg.backChannel.close()
                    }
                }
            }
        }
        return CommonConnectionScope(SessionChannel(job, channel))
    }

    private class CommonConnectionScope(override val dbSessionChannel: SessionChannel) : ConnectionScope

    operator fun Boolean.invoke(): Value<Boolean> = Value.of(this)
    operator fun Long.invoke(): Value<Long> = Value.of(this)
    operator fun Int.invoke(): Value<Long> = Value.of(this.toLong())
    operator fun Keyword.invoke(): Value<Keyword> = Value.of(this)
    operator fun String.invoke(): Value<String> = Value.of(this)
    operator fun Date.invoke(): Value<Date> = Value.of(this)
    operator fun Double.invoke(): Value<Double> = Value.of(this)
    operator fun BigDecimal.invoke(): Value<BigDecimal> = Value.of(this)
    operator fun AnyValue.invoke(): Value<AnyValue> = Value.of(this)
    operator fun <T : Any> Value<T>.invoke(): Value<AnyValue> = Value.of(AnyValue(this))
    operator fun TagList.invoke(): Value<TagList> = Value.of(this)

    operator fun <T : Any> Keyword.rangeTo(value: Value<T>) = tagOf(value, this)
    operator fun Keyword.rangeTo(v: Boolean) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: Long) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: Int) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: Keyword) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: String) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: Date) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: Double) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: BigDecimal) = tagOf(v(), this)
    operator fun Keyword.rangeTo(v: TagList) = tagOf(v(), this)
}


interface DestructuringScope {
    operator fun Map<Keyword, *>.get(attr: Attribute) = this[attr.attrName]
}