package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.UnknownInstructionException
import io.github.marco4413.kwasm.bytecode.U8
import io.github.marco4413.kwasm.bytecode.WasmInputStream
import io.github.marco4413.kwasm.bytecode.WasmOutputStream
import io.github.marco4413.kwasm.runtime.Configuration
import io.github.marco4413.kwasm.runtime.Stack

fun createInstructionMap(vararg descriptor: InstructionDescriptor) : MutableMap<U8, InstructionDescriptor> {
    val map = mutableMapOf<U8, InstructionDescriptor>()
    descriptor.forEach { map[it.opcode] = it }
    return map
}

abstract class InstructionDescriptor(val name: String, val opcode: U8) {
    abstract fun read(s: WasmInputStream) : Instruction
    override fun toString(): String = "$name#$opcode"
}

abstract class Instruction(val descriptor: InstructionDescriptor) {
    companion object {
        val instructions = createInstructionMap(
            // Control
            UnreachableDescriptor, NopDescriptor,
            BlockDescriptor, LoopDescriptor, IfDescriptor,
            BrDescriptor, BrIfDescriptor, BrTableDescriptor,
            ReturnDescriptor, CallDescriptor,
            // Parametric
            SelectDescriptor,
            // Local
            LocalGetDescriptor, LocalSetDescriptor, LocalTeeDescriptor,
            // Global
            GlobalSetDescriptor,
            // I32
            I32ConstDescriptor,
            I32EqZDescriptor,
            I32LTUDescriptor, I32GTUDescriptor, I32LEUDescriptor,
            I32AddDescriptor, I32SubDescriptor, I32MulDescriptor,
            I32AndDescriptor, I32SHLDescriptor, I32SHRSDescriptor,
            // F64
            F64ConstDescriptor,
            F64MulDescriptor,
            F64ConvertI32UDescriptor,
            // Memory
            I32Load8UDescriptor, I32StoreDescriptor,
            F64StoreDescriptor,
            MemoryRelatedDescriptor
        )

        fun read(s: WasmInputStream, _opcode: U8? = null) : Instruction {
            // TODO: Move this to Module.kt?
            val opcode = _opcode ?: s.readU8()
            val descriptor = instructions[opcode]
                ?: throw UnknownInstructionException(opcode)
            return descriptor.read(s)
        }
    }

    abstract fun execute(config: Configuration, stack: Stack)
    open fun write(s: WasmOutputStream) { s.writeU8(descriptor.opcode) }
}
