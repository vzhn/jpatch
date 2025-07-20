package me.vzhilin.jpatch

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonPatchTests {
    @Test
    fun removeFromRootArray() {
        testRemoveAt("[0,1,2]", "/0", "[1,2]")
        testRemoveAt("[0,1,2]", "/1", "[0,2]")
        testRemoveAt("[0,1,2]", "/2", "[0,1]")
        testRemoveAt("[0]", "/0", "[]")
        testRemoveAt("[0]", "/-", "[]")
        testRemoveAt("[[0,1,2]]", "", "null")
    }

    @Test
    fun removeFromNestedArray() {
        testRemoveAt("[[0,1,2]]", "/0/0", "[[1,2]]")
        testRemoveAt("[[0,1,2]]", "/0/1", "[[0,2]]")
        testRemoveAt("[[0,1,2]]", "/0/2", "[[0,1]]")
        testRemoveAt("[[0]]", "/0/0", "[[]]")
        testRemoveAt("[[0,1,2]]", "/0", "[]")
    }

    @Test
    fun removeFromArrayInsideObject() {
        testRemoveAt("{'a':[1,2,3]}", "/a/0", "{'a':[2,3]}")
        testRemoveAt("{'a':[1]}", "/a/0", "{'a':[]}")
    }

    @Test
    fun addElementToTheArray() {
        testAddAt("[]", "/0", "1", "[1]")
        testAddAt("[2]", "/0", "1", "[1,2]")
        testAddAt("[2]", "/1", "1", "[2,1]")
        testAddAt("[2]", "/-", "1", "[2,1]")
    }

    @Test
    fun addElementToTheNestedArray() {
        testAddAt("[[]]", "/0/-", "1", "[[1]]")
        testAddAt("[[1]]", "/0/-", "2", "[[1,2]]")
        testAddAt("[[1]]", "/0/0", "2", "[[2,1]]")
        testAddAt("[[1]]", "/0/1", "2", "[[1,2]]")
    }

    @Test
    fun addElementInsideOfObject() {
        testAddAt("{'a':[0]}", "/a/0", "1", "{'a':[1,0]}")
        testAddAt("{'a':[0]}", "/a/1", "1", "{'a':[0,1]}")
        testAddAt("{'a':[0]}", "/a/-", "1", "{'a':[0,1]}")
    }

    @Test
    fun removeElementInTheObject() {
        testRemoveAt("{'k1': 1, 'k2': 2}", "/k1", "{'k2': 2}")
        testRemoveAt("{'k1': 1, 'k2': 2}", "/k2", "{'k1': 1}")
        testRemoveAt("{'k1': 1}", "/k1", "{}")
        testRemoveAt("{'k1': 1}", "", "null")
    }

    @Test
    fun removeElementDeeplyInTheObject() {
        testRemoveAt("{'k1': {'k2': {'k3': 'v3'}}}", "/k1/k2/k3", "{'k1': {'k2': {}}}")
        testRemoveAt("{'k1': {'k2': {'k3': 'v3'}}}", "/k1/k2", "{'k1': {}}")
        testRemoveAt("{'k1': {'k2': {'k3': 'v3'}}}", "/k1", "{}")
    }

    @Test
    fun replaceElement() {
        testReplaceAt("[1]", "/0", "2", "[2]")
        testReplaceAt("[1]", "", "[1,2,3]", "[1,2,3]")
        testReplaceAt("[[1,2], [3,4]]", "/1/1", "5", "[[1,2],[3,5]]")
        testReplaceAt("{'1':'2'}", "/1", "'3'", "{'1':'3'}")
        testReplaceAt("{'1':'2'}", "/1", "{'k':'v'}", "{'1':{'k':'v'}}")
        testReplaceAt("{'k1': {'k2': {'k3': 'v3'}}}", "/k1/k2/k3", "'v4'", "{'k1': {'k2': {'k3': 'v4'}}}")
    }

    @Test
    fun replaceUndefined() {
        testReplaceAt("null", "", "'abc'", "'abc'")
        testReplaceAt("null", "", "[1,2]", "[1,2]")
        testReplaceAt("null", "", "{'1':'2'}", "{'1':'2'}")
    }

    fun testRemoveAt(current: String, path: String, expect: String) {
        assertEquals(pj(expect), pj(current).removeAt(path))
    }

    fun testAddAt(current: String, path: String, e: String, expect: String) {
        assertEquals(pj(expect), pj(current).addAt(path, pj(e)))
    }

    fun testReplaceAt(current: String, path: String, e: String, expect: String) {
        assertEquals(pj(expect), pj(current).replaceAt(path, pj(e)))
    }

    fun pj(json: String): JsonElement {
        return Json.Default.parseToJsonElement(json.replace('\'', '"'))
    }
}