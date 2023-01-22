package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Expression

typealias Label = Expression

class Frame(val locals: MutableList<Value>, val module: ModuleInstance)
class ComputationThread(val frame: Frame, val instructions: Expression)
class Configuration(val store: Store, val thread: ComputationThread)
