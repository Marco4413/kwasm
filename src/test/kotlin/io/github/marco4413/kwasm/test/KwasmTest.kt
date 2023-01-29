package io.github.marco4413.kwasm.test

import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.runtime.*
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.OutputStream
import java.lang.NullPointerException
import java.lang.StringBuilder
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun openResource(name: String) : InputStream {
    val resource = Module.Companion::class.java.getResourceAsStream(name)
    assertNotNull(resource)
    return resource
}

class KwasmTest {
    @Test
    fun testPrintString() {
        println("TEST: Print String")
        val module = Module.fromStream(openResource("/print_string.wasm"))
        val store = Store()

        val buffer = StringBuilder()
        val cPrint = store.allocateHostFunction(FunctionType(listOf(ValueType.I32), listOf())) {
            config, _ ->
            val chValue = config.thread.frame.locals[0]
            assertEquals(ValueType.I32, chValue.type)
            chValue as ValueI32
            buffer.append(chValue.value.toChar())
        }

        val instance = ModuleInstance(store, module, mapOf(
            "env/c_print" to ExternalValue(ExternalType.FunctionAddress, cPrint)
        ))

        val print = instance.exports["print"]
            ?: throw NullPointerException()
        instance.invoke(print, listOf(ValueI32(0)))

        assertEquals("Hello World!", buffer.toString())
    }

    @Test
    fun testWriteModule() {
        println("TEST: Write Module")
        val fileBytes = openResource("/print_string.wasm").readBytes()
        val module = Module.fromStream(openResource("/print_string.wasm"))

        val moduleBytes = ArrayList<Byte>()
        val stream = object : OutputStream() {
            override fun write(b: Int) { moduleBytes.add(b.toByte()) }
        }

        module.write(stream)
        assertContentEquals(fileBytes, moduleBytes.toByteArray())
    }
}