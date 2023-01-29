package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.*

enum class DataModeType { Passive, Active }
open class DataMode(val type: DataModeType)
class DataModePassive : DataMode(DataModeType.Passive)
class DataModeActive(val memory: MemoryIdx, val offset: Expression) : DataMode(DataModeType.Active)

class Data(val init: List<U8>, val mode: DataMode)

const val DataSectionId: U8 = 11u
typealias DataSection = List<Data>

fun readDataSection(s: WasmInputStream) : DataSection {
    s.readU32() // SIZE
    return s.readVector {
        val mode = when (s.readU32()) {
            0u -> DataModeActive(0u, readExpression(s))
            1u -> DataModePassive()
            2u -> DataModeActive(s.readU32(), readExpression(s))
            else -> throw IllegalStateException()
        }
        Data(s.readVector { s.readU8() }, mode)
    }
}

fun writeDataSection(s: WasmOutputStream, sec: DataSection) {
    s.writeU8(DataSectionId)
    s.writeSize {
        s.writeVector(sec) {
            _, data ->
            when (data.mode.type) {
                DataModeType.Passive ->
                    s.writeU8(1u)
                DataModeType.Active -> {
                    data.mode as DataModeActive
                    if (data.mode.memory == 0u) {
                        s.writeU8(0u)
                    } else {
                        s.writeU8(2u)
                        s.writeU32(data.mode.memory)
                    }
                    writeExpression(s, data.mode.offset)
                }
            }
            s.writeVector(data.init) { _, byte -> s.writeU8(byte) }
        }
    }
}
