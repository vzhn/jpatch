package me.vzhilin.jpatch

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun main() {
    val a = JsonNull
    val b = a.replaceAt("", JsonObject(emptyMap()))
    val c = b.addAt("/key", JsonPrimitive("text_value"))
    val d = c.replaceAt("/key", JsonArray(emptyList()))
    val e = d.addAt("/key/0", JsonPrimitive("array_item_1"))
    val f = e.addAt("/key/-", JsonPrimitive("array_item_2"))

    // {"key":["array_item_1","array_item_2"]}
    println(f)

    // "array_item_2"
    println(f.elementAt("/key/1"))
}