package com.rubyhuntersky.tomedb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

//  enum class Counter : Attribute {
//        Count {
//            override val valueType: ValueType = ValueType.LONG
//            override val cardinality: Cardinality = Cardinality.ONE
//            override val description: String = "The current count of a counter"
//        }
//    }
//
//    private lateinit var dataScope: DataScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        dataScope = dataScope(File(filesDir, "tome"), Counter.values().toList())
//        dataScope.connect {
//            refreshDb {
//                val slot = slot("counter")
//                val result = find { rules = listOf(-slot, slot capture Counter.Count) }
//                val counter = slot(result).first()
//                Log.i(this::class.java.simpleName, "COUNTER: $counter")
//            }
//        }
    }
}
