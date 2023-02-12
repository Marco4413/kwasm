package io.github.marco4413.kwasm.test

import io.github.marco4413.kwasm.bytecode.BlockType
import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.ValueType
import io.github.marco4413.kwasm.bytecode.section.*
import io.github.marco4413.kwasm.instructions.*
import io.github.marco4413.kwasm.runtime.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.NullPointerException
import java.lang.StringBuilder
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

fun openResource(name: String) : InputStream {
    val resource = Module.Companion::class.java.getResourceAsStream(name)
    assertNotNull(resource)
    return resource
}

fun time(label: String, func: () -> Unit) {
    val startTime = System.nanoTime()
    func()
    val endTime = System.nanoTime()
    println("$label took ${(endTime-startTime) / 1e6}ms")
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
        val result = instance.invoke(print, listOf(ValueI32(0)))

        assertNull(result.trap)
        assertEquals("Hello World!", buffer.toString())
    }

    @Test
    fun testPrintString() {
        println("TEST: Print String")
        val module = Module.read(openResource("/print_string.wasm"))
        runPrintString(module)
    }

    @Test
    fun testWriteModule() {
        println("TEST: Write Module")
        val fileBytes = openResource("/factorial.wasm").readBytes()
        val module = Module.read(openResource("/factorial.wasm"))

        val stream = ByteArrayOutputStream()
        module.write(stream)

        assertContentEquals(fileBytes, stream.toByteArray())
    }

    @Test
    fun testCompilePrintString() {
        println("TEST: Compile Print String")
        val stream = ByteArrayOutputStream()
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
                    Block(BlockType.Void, listOf(
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
        ).write(stream)

        val module = Module.read(stream.toByteArray().inputStream())
        runPrintString(module)
    }

    @Test
    fun testFactorialGen() {
        println("TEST: Factorial Gen")
        val genCount = 25
        val expectedResults = listOf(
            4607182418800017408L, 4607182418800017408L, 4611686018427387904L, 4618441417868443648L, 4627448617123184640L,
            4638144666238189568L, 4649544402794971136L, 4662263553305083904L, 4675774352187195392L, 4689977843394805760L,
            4705047200009289728L, 4720626352061939712L, 4736815922046566400L, 4753323511810883584L, 4770521722250067968L,
            4788179038478794752L, 4806193436988276736L, 4824542600135966720L, 4843268373501640704L, 4862483217080946688L,
            4882150158176967168L, 4901649482228549152L, 4922002638466838636L, 4942108792695858382L, 4963085890558262170L
        )

        val module = Module.read(openResource("/factorial.wasm"))
        val store = Store()

        val instance = ModuleInstance(store, module, mapOf())
        val genFactorial = instance.exports["gen_factorial"] ?: throw NullPointerException()

        time("gen_factorial") {
            instance.invoke(genFactorial, listOf(ValueI32(0), ValueI32(genCount)))
        }

        val memoryExport = instance.exports["memory"] ?: throw NullPointerException()
        assert(memoryExport.value.type == ExternalType.MemoryAddress)
        val memory = store.getMemory(memoryExport.value.address)

        val results = List(genCount) { memory.getI64(it * 8) }
        assertContentEquals(expectedResults, results)
    }
}