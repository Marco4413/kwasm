package io.github.marco4413.kwasm.test

import io.github.marco4413.kwasm.bytecode.Module

fun main(args: Array<String>) {
    if (args.isEmpty())
        throw NullPointerException("Resource to open not specified.")
    val resourceName = args[0]
    val module = Module.fromStream(
        Module.Companion::class.java.getResourceAsStream(resourceName)
            ?: throw NullPointerException("Couldn't open resource '$resourceName'")
    )
}
