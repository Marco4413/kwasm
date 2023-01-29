package io.github.marco4413.kwasm.test

import io.github.marco4413.kwasm.bytecode.BlockType
import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.section.*
import io.github.marco4413.kwasm.instructions.*
import io.github.marco4413.kwasm.runtime.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
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
    private fun runPrintString(module: Module) {
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
    fun testPrintString() {
        println("TEST: Print String")
        val module = Module.fromStream(openResource("/print_string.wasm"))
        runPrintString(module)
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

    @Test
    fun testCompilePrintString() {
        println("TEST: Compile Print String")

        val oStream = ByteArrayOutputStream()
        Module(Module.WASM_MAGIC, Module.WASM_VERSION,
            listOf(),
            listOf(FunctionType(listOf(ValueType.I32), listOf())),
            listOf(Import("env", "c_print", ImportDescriptionFunction(0u))),
            listOf(0u),
            listOf(Memory(MemoryType(1u))),
            listOf(
                Export("memory", ExportDescription(ExportType.Memory, 0u)),
                Export("print", ExportDescription(ExportType.Function, 1u))
            ),
            null,
            listOf(Function(listOf(Locals(1u, ValueType.I32)), listOf(
                Loop(BlockType.Void, listOf(
                    InstrBlock(BlockType.Void, listOf(
                        LocalGet(0u),
                        I32Load8U(MemoryArgument(0u, 0u)),
                        LocalTee(1u),
                        I32EqZ(),
                        BrIf(0u),
                        LocalGet(1u),
                        Call(0u),
                        LocalGet(0u),
                        I32Const(1),
                        I32Add(),
                        LocalSet(0u),
                        Br(1u)
                    ))
                ))
            ))),
            listOf(Data(
                listOf(72u, 101u, 108u, 108u, 111u, 32u, 87u, 111u, 114u, 108u, 100u, 33u, 0u),
                DataModeActive(0u, listOf(I32Const(0)))
            ))
        ).write(oStream)

        val module = Module.fromStream(oStream.toByteArray().inputStream())
        runPrintString(module)
    }
}