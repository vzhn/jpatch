package me.vzhilin.jpatch

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

fun JsonElement.addAt(path: String, e: JsonElement): JsonElement = addAt(splitPath(path), e)
fun JsonElement.removeAt(path: String): JsonElement = removeAt(splitPath(path))
fun JsonElement.replaceAt(path: String, e: JsonElement): JsonElement = replaceAt(splitPath(path), e)
fun JsonElement.hasElement(path: String): Boolean = hasElement(splitPath(path))
fun JsonElement.elementAt(path: String): JsonElement? = elementAt(splitPath(path))

private fun splitPath(path: String): List<String> = path.split('/')

private fun JsonElement.replaceAt(path: List<String>, e: JsonElement) = updateAt(path) { e }

private fun JsonElement.updateAt(path: List<String>, updater: (e: JsonElement) -> JsonElement): JsonElement {
    if (path == listOf("")) {
        return updater(this)
    }
    return if (path.size == 1) {
        updater(this)
    } else {
        val next = path[1]
        val tail = path.subList(1, path.size)
        when (this) {
            is JsonArray -> {
                val index = next.toInt()
                set(index, get(index).updateAt(tail, updater))
            }
            is JsonObject -> add(next, getValue(next).updateAt(tail, updater))
            else -> throw IllegalArgumentException("Not container node in the middle of the path")
        }
    }
}

private fun JsonElement.removeAt(path: List<String>): JsonElement {
    if (path.isEmpty()) throw RuntimeException("path should not be empty")
    if (path[0] !== "") throw RuntimeException("the first item in path should be empty")
    if (path.size == 1) return JsonNull

    val head = path.subList(0, path.size - 1)
    val key = path.last()
    return updateAt(head) { e ->
        when(e) {
            is JsonObject -> e.remove(key)
            is JsonArray -> {
                if (key == "-") {
                    e.remove(e.size - 1)
                } else {
                    key.toIntOrNull()?.let(e::remove) ?: throw RuntimeException("key $key is not number")
                }
            }
            else -> throw IllegalArgumentException("Not container node in the middle of the path")
        }
    }
}

private fun JsonElement.addAt(path: List<String>, e: JsonElement): JsonElement {
    if (path == listOf("")) return e

    val head = path.subList(0, path.size - 1)
    val index = path.last()
    return updateAt(head) {
        when(it) {
            is JsonArray -> if (index == "-") {
                it.add(e)
            } else {
                it.insert(index.toInt(), e)
            }

            is JsonObject -> it.add(index, e)
            else -> throw IllegalArgumentException("Not container node in the middle of the path")
        }
    }
}

private fun JsonElement.elementAt(path: List<String>): JsonElement? {
    if (path.isEmpty()) throw RuntimeException("path should not be empty")
    if (path[0] !== "") throw RuntimeException("the first item in path should be empty")
    if (path.size == 1) return JsonNull

    return getAt(path.subList(1, path.size))
}

private fun JsonElement.getAt(path: List<String>): JsonElement? {
    return if (path.isEmpty()) {
        this
    } else {
        val current = path.first()
        val tail = path.subList(1, path.size)

        when (this) {
            is JsonArray -> {
                val index = current.toIntOrNull() ?: return null
                return if (index >= 0 && index < this.size) {
                    get(index).getAt(tail)
                } else {
                    null
                }
            }
            is JsonObject -> {
                return get(current)?.getAt(tail)
            }
            else -> throw IllegalArgumentException("Not container node in the middle of the path")
        }
    }
}


private fun JsonElement.hasElement(path: List<String>): Boolean {
    if (path.isEmpty()) {
        throw IllegalArgumentException("path should not be empty")
    }

    if (path[0] != "") {
        throw IllegalArgumentException("the first element should be \"\"")
    }

    fun JsonElement.checkTail(t: List<String>): Boolean {
        if (t.isEmpty()) {
            return true
        } else {
            val head = t[0]
            val tail = t.subList(1, t.size)

            return when (this) {
                is JsonArray -> {
                    val index = Integer.parseInt(head) // todo parse error
                    return if (index >= 0 && index < this.size) {
                        this[index].checkTail(tail)
                    } else {
                        false
                    }
                }
                is JsonObject -> {
                    return if (this.containsKey(head)) {
                        this.getValue(head).checkTail(tail)
                    } else {
                        false
                    }
                }
                else -> true
            }
        }
    }

    return checkTail(path.subList(1, path.size))
}

private fun JsonObject.add(key: String, value: JsonElement): JsonObject {
    return JsonObject(this + (key to value))
}

private fun JsonObject.remove(key: String): JsonObject {
    return JsonObject(this - key)
}

private fun JsonArray.set(index: Int, value: JsonElement): JsonArray {
    if (get(index) === value) return this

    return JsonArray(subList(0, index) + value + subList(index + 1, this.size))
}

private fun JsonArray.add(v: JsonElement?): JsonArray {
    return JsonArray(this + (v ?: JsonNull))
}

private fun JsonArray.remove(index: Int): JsonArray {
    if (isEmpty()) return this
    return when (index) {
        0 -> JsonArray(subList(1, size))
        this.size - 1 -> JsonArray(subList(0, index))
        else -> JsonArray(subList(0, index) + subList(index + 1, size))
    }
}

private fun JsonArray.insert(index: Int, v: JsonElement?): JsonArray {
    if (index < 0 || index > size) throw ArrayIndexOutOfBoundsException()

    val value = v ?: JsonNull
    val idx = 0.coerceAtLeast(index).coerceAtMost(size)
    return JsonArray(subList(0, idx) + value + subList(idx, size))
}