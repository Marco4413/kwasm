package io.github.marco4413.kwasm.runtime

open class Trap(val config: Configuration, val message: String)
class UnreachableTrap(config: Configuration) : Trap(config, "Unreachable")
class MemoryIndexOutOfBoundsTrap(config: Configuration) : Trap(config, "Memory index out of bounds")
class DataIndexOutOfBoundsTrap(config: Configuration) : Trap(config, "Data index out of bounds")
