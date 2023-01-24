package io.github.marco4413.kwasm.runtime

import io.github.marco4413.kwasm.*
import io.github.marco4413.kwasm.bytecode.Address
import io.github.marco4413.kwasm.bytecode.Module
import io.github.marco4413.kwasm.bytecode.Name
import io.github.marco4413.kwasm.bytecode.section.*
import io.github.marco4413.kwasm.instructions.DataDrop
import io.github.marco4413.kwasm.instructions.I32Const
import io.github.marco4413.kwasm.instructions.MemoryInit
import java.lang.IllegalStateException
import kotlin.collections.ArrayList

class ExportInstance(val name: Name,
                     val value: ExternalValue)

class ModuleInstance(val store: Store, module: Module, imports: List<ExternalValue>) {
    val types: List<FunctionType>
    val functions: List<Address>
    val memories: List<Address>
    val globals: List<Address>
    val data: List<Address>
    val exports: List<ExportInstance>

    init {
        types = module.types
        functions = ArrayList()
        memories = ArrayList()
        globals = ArrayList()
        data = ArrayList()

        // IMPORTS
        if (imports.size != module.imports.size)
            throw WrongImportCount(module.imports.size.toUInt(), imports.size.toUInt())

        for (i in imports.indices) {
            val externValue = imports[i]
            val importDesc = module.imports[i].description
            when (externValue.type) {
                ExternalType.FunctionAddress -> {
                    if (importDesc.type != ImportType.Function)
                        throw InvalidImportType(importDesc.type, ExternalType.FunctionAddress, i.toUInt())
                    val expectedType = module.types[
                            (importDesc as ImportDescriptionFunction).typeIdx.toInt()]
                    val function = store.getFunction(externValue.address)
                    if (!expectedType.signatureEquals(function.type))
                        throw InvalidImportFunctionType(expectedType, function.type, i.toUInt())
                    functions.add(externValue.address)
                }
                ExternalType.TableAddress -> TODO()
                ExternalType.MemoryAddress -> TODO()
                ExternalType.GlobalAddress -> {
                    if (importDesc.type != ImportType.Global)
                        throw InvalidImportType(importDesc.type, ExternalType.GlobalAddress, i.toUInt())
                    val expectedType = (importDesc as ImportDescriptionGlobal).value.type
                    val global = store.getGlobal(externValue.address)
                    if (global.value.type != expectedType)
                        throw InvalidImportGlobalType(expectedType, global.value.type, i.toUInt())
                    globals.add(externValue.address)
                }
            }
        }

        // INSTANTIATION
        functions.addAll(module.functions.mapIndexed {
            i, it -> store.allocateFunction(it, this, module.code[i])
        })

        // TODO: Module-specific Tables

        memories.addAll(module.memories.map {
            store.allocateMemory(it.type)
        })

        // TODO: Globals, Elements

        data.addAll(module.data.map {
            store.allocateData(it.init)
        })

        exports = ArrayList(module.exports.map {
            when (it.description.type) {
                ExportType.Function -> ExportInstance(it.name,
                    ExternalValue(ExternalType.FunctionAddress, functions[it.description.idx.toInt()]))
                ExportType.Memory -> ExportInstance(it.name,
                    ExternalValue(ExternalType.MemoryAddress, memories[it.description.idx.toInt()]))
                else -> TODO("Unsupported Export Type ${it.description.type}.")
            }
        })

        // INITIALIZATION (Or at least the stuff that still needs to be initialized)
        val config = Configuration(store, ComputationThread(Frame(mutableListOf(), this), listOf()))
        val stack = Stack()
        stack.pushFrame(config.thread.frame)

        for (i in module.data.indices) {
            val data = module.data[i]
            if (data.mode.type != DataModeType.Active) continue
            data.mode as DataModeActive
            assert(data.mode.memory == 0u)

            val offsetLabel = Label(data.mode.offset)
            stack.pushLabel(offsetLabel)
            executeLabel(offsetLabel, config, stack)

            val offset = stack.popValue()
            val initLabel = Label(listOf(
                I32Const(0),
                I32Const(data.init.size),
                MemoryInit(i.toUInt()),
                DataDrop(i.toUInt())
            ))

            stack.pushLabel(initLabel)
            stack.pushValue(offset)
            executeLabel(initLabel, config, stack)
        }

        stack.popFrame()
        if (module.start != null) invoke(functions[module.start.toInt()], stack)
    }

    fun executeLabel(label: Label, config: Configuration, stack: Stack) {
        for (instr in label)
            // println(instr.descriptor.name)
            instr.execute(config, stack)
        assert(stack.popLastLabel() == label)
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

    fun invoke(addr: Address, stack: Stack) {
        val func = store.functions[addr.toInt()]

        val locals = MutableList(func.type.parameters.size) {
            val expectedType = func.type.parameters[it]
            val value = stack.popValue()
            if (value.type != expectedType)
                throw InvalidArgumentTypeException(addr, it.toUInt(), expectedType, value.type)
            value
        }

        when (func) {
            is ModuleFunctionInstance -> {
                for (i in func.code.locals.indices) {
                    val codeLocals = func.code.locals[i]
                    for (j in 0u until codeLocals.count)
                        locals.add(getDefaultForValueType(codeLocals.type))
                }

                val config = Configuration(store, ComputationThread(Frame(locals, this), func.code.body))
                stack.pushFrame(config.thread.frame)

                val label = Label(config.thread.instructions)
                stack.pushLabel(label)
                executeLabel(label, config, stack)
                if (stack.lastFrame == config.thread.frame)
                    stack.popLastFrame()
            }
            is HostFunctionInstance -> {
                val config = Configuration(store, ComputationThread(Frame(locals, this), listOf()))
                stack.pushFrame(config.thread.frame)
                func.code(config, stack)
                stack.popLastFrame()
            }
            else -> throw IllegalStateException("Invoking an unsupported Function Instance")
        }
    }
}
