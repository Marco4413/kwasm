package io.github.marco4413.kwasm.bytecode.section

import io.github.marco4413.kwasm.bytecode.*

class Custom(val name: Name, val data: List<U8>)

const val CustomSectionId: U8 = 0u
typealias CustomSection = List<Custom>

fun readCustomSection(s: WasmInputStream) : CustomSection {
    val bytes = s.readVector { s.readU8().toByte() }
    val cStream = WasmInputStream(bytes.toByteArray().inputStream())
    // custom ::== name byte*
    val name = cStream.readName()
    val data = ArrayList<U8>()
    while (cStream.available() > 0) data.add(cStream.readU8())

    return listOf(Custom(name, data))
}

fun writeCustomSection(s: WasmOutputStream, sec: CustomSection) {
    for (custom in sec) {
        s.writeU8(CustomSectionId)
        s.writeSize {
            s.writeName(custom.name)
            for (byte in custom.data)
                s.writeU8(byte)
        }
    }
}
