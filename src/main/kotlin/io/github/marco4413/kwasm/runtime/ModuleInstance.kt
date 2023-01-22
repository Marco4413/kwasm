package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.bytecode.Address
import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.Name
import io.github.marco4413.kwasm.bytecode.section.ExportType
import io.github.marco4413.kwasm.bytecode.section.FunctionType
import io.github.marco4413.kwasm.bytecode.section.ImportDescriptionGlobal
import io.github.marco4413.kwasm.bytecode.section.ImportType

class ExportInstance(val name: Name,
                     val value: ExternalValue)

class ModuleInstance(val store: Store, module: Module, imports: List<ExternalValue>) {
    val types: List<FunctionType>
    val functionAddresses: List<Address>
    val globalAddresses: List<Address>
    val exports: List<ExportInstance>

    init {
        types = module.types
        functionAddresses = ArrayList()
        globalAddresses = ArrayList()

        // IMPORTS
        if (imports.size != module.imports.size)
            throw IllegalArgumentException("Not enough imports provided.")

        for (i in imports.indices) {
            val externValue = imports[i]
            val importDesc = module.imports[i].description
            when (externValue.type) {
                ExternalType.FunctionAddress -> TODO()
                ExternalType.TableAddress -> TODO()
                ExternalType.MemoryAddress -> TODO()
                ExternalType.GlobalAddress -> {
                    if (importDesc.type != ImportType.Global)
                        throw IllegalArgumentException("Import at $i is a Global, expected ${importDesc.type}")
                    val expectedGlobalType = (importDesc as ImportDescriptionGlobal).value.type
                    val global = store.getGlobal(externValue.address)
                    if (global.value.type != expectedGlobalType)
                        throw IllegalArgumentException("Import at $i is a Global of type ${global.value.type}, expected $expectedGlobalType")
                    globalAddresses.add(externValue.address)
                }
            }
        }

        functionAddresses.addAll(module.functions.map {
            store.allocateFunction(module, it, this)
        })
        // END IMPORTS

        // TODO: Module-specific Tables, Memories, Globals

        exports = ArrayList(module.exports.map {
            when (it.description.type) {
                ExportType.Type -> ExportInstance(it.name,
                    ExternalValue(ExternalType.FunctionAddress, functionAddresses[it.description.idx.toInt()]))
                else -> TODO("Only functions are supported as exports.")
            }
        })

        // TODO: Elements, Data

        if (module.start != null) invoke(functionAddresses[module.start.toInt()])
    }

    fun invoke(export: ExportInstance, params: List<Value>) : List<Value> {
        if (export.value.type != ExternalType.FunctionAddress)
            throw InvalidExternalType(ExternalType.FunctionAddress, export.value.type)
        println("Invoking ${export.name}")
        return invoke(export.value.address, params)
    }

    fun invoke(addr: Address, params: List<Value>) : List<Value> {
        val f = store.functions[addr.toInt()]
        if (params.size != f.type.parameters.size)
            throw WrongArgumentCount(addr, f.type.parameters.size.toUInt(), params.size.toUInt())

        for (i in params.indices) {
            val expectedType = f.type.parameters[i]
            if (params[i].type != expectedType)
                throw InvalidArgumentTypeException(addr, i.toUInt(), expectedType, params[i].type)
        }

        val stack = Stack()
        for (v in params) stack.pushValue(v)
        invoke(addr, stack)

        return List(f.type.results.size) {
            val index = f.type.results.size - it - 1
            val expectedType = f.type.results[index]
            val value = stack.popValue()
            if (value.type != expectedType)
                throw InvalidResultTypeException(addr, index.toUInt(), expectedType, value.type)
            value
        }
    }

    private fun runLabel(label: Label, config: Configuration, stack: Stack) {
        val depth = stack.labelCount
        for (instr in label) {
            println(instr.descriptor.name)
            instr.execute(config, stack)

            if (stack.lastFrame != config.thread.frame) {
                break
            } else if (stack.labelCount > depth) {
                runLabel(stack.lastLabel!!, config, stack)
                if (stack.lastFrame != config.thread.frame)
                    break
            } else if (stack.labelCount < depth) return
        }
        stack.popLastLabel()
    }

    fun invoke(addr: Address, stack: Stack = Stack()) {
        val f = store.functions[addr.toInt()]

        val locals = MutableList(f.type.parameters.size) {
            val expectedType = f.type.parameters[it]
            val value = stack.popValue()
            if (value.type != expectedType)
                throw InvalidArgumentTypeException(addr, it.toUInt(), expectedType, value.type)
            value
        }

        for (i in f.code.locals.indices) {
            val codeLocals = f.code.locals[i]
            for (j in 0u until codeLocals.count)
                locals.add(getDefaultForValueType(codeLocals.type))
        }

        val config = Configuration(store, ComputationThread(Frame(locals, this), f.code.body))
        stack.pushFrame(config.thread.frame)

        stack.pushLabel(config.thread.instructions)
        runLabel(config.thread.instructions, config, stack)
        if (stack.lastFrame == config.thread.frame)
            stack.popLastFrame()
    }
}
