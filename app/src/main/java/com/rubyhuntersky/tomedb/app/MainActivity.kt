package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rubyhuntersky.tomedb.Attribute
import com.rubyhuntersky.tomedb.Cardinality
import com.rubyhuntersky.tomedb.Update
import com.rubyhuntersky.tomedb.basics.ValueType
import com.rubyhuntersky.tomedb.basics.invoke
import com.rubyhuntersky.tomedb.datascope.dataScope
import java.io.File

class MainActivity : AppCompatActivity() {

    enum class Counter : Attribute {
        Count {
            override val valueType: ValueType = ValueType.LONG
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The current count of a counter"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dataScope = dataScope(File(filesDir, "tome"), Counter.values().toList())
        dataScope.connect {
            checkoutLatest {
                val slot = slot("counter")
                val result = find { rules = listOf(-slot, slot capture Counter.Count) }
                val counter = slot(result).firstOrNull()
                Log.i(this::class.java.simpleName, "COUNTER: $counter")
                if (counter == null) {
                    sendUpdate(
                        setOf(
                            Update(1000, Counter.Count, 33())
                        )
                    )
                }
            }
        }
    }
}
