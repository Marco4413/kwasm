package io.github.marco4413.kwasm.instructions

import io.github.marco4413.kwasm.U8
import io.github.marco4413.kwasm.WasmContext
import io.github.marco4413.kwasm.WasmInputStream
import java.io.OutputStream

const val BlockElse: U8 = 0x05u
const val BlockEnd: U8 = 0x0Bu

abstract class InstructionDescriptor(val name: String, val opcode: U8) {
    abstract fun read(s: WasmInputStream) : Instruction
    override fun toString(): String = "$name#$opcode"
}

abstract class Instruction(val descriptor: InstructionDescriptor) {
    companion object {
        private val instr = mutableMapOf(
            NopDescriptor.opcode to NopDescriptor
        )

        fun fromStream(s: WasmInputStream) : Instruction {
            val opcode = s.readU8()
            val descriptor = instr[opcode]
                ?: throw NullPointerException("No instruction with opcode 0x${opcode.toString(16)} found.")
            return descriptor.read(s)
        }
    }

    fun execute(ctx: WasmContext) { }
    fun write(s: OutputStream) {
        s.write(descriptor.opcode.toInt())
    }
}
