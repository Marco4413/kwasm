package io.github.marco4413.kwasm.test

import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.runtime.*
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.lang.StringBuilder
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

        val instance = ModuleInstance(store, module, listOf(
            ExternalValue(ExternalType.FunctionAddress, cPrint)
        ))

        val print = instance.exports[1] // 0 is Memory
        instance.invoke(print, listOf(ValueI32(0)))

        assertEquals("Hello World!", buffer.toString())
    }
}