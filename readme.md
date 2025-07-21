# JPatch
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)

Routines for immutable `JsonElement` patching


```kotlin
fun JsonElement.addAt(path: String, e: JsonElement): JsonElement 
fun JsonElement.removeAt(path: String): JsonElement
fun JsonElement.replaceAt(path: String, e: JsonElement): JsonElement
fun JsonElement.hasElement(path: String): Boolean
fun JsonElement.elementAt(path: String): JsonElement?
```

### Usage example
```kotlin
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
```