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

class InvocationResult(val results: List<Value>, val trap: Trap?)
class ExportInstance(val name: Name, val value: ExternalValue)

class ModuleInstance(val store: Store, module: Module, imports: Map<Name, ExternalValue>) {
    val types: List<FunctionType>
    val functions: List<Address>
    val memories: List<Address>
    val globals: List<Address>
    val data: List<Address>
    val exports: Map<Name, ExportInstance>

    init {
        types = module.types
        functions = ArrayList()
        memories = ArrayList()
        globals = ArrayList()
        data = ArrayList()
        exports = HashMap()

        // IMPORTS
        if (imports.size < module.imports.size)
            throw NotEnoughImportsException(module.imports.size.toUInt(), imports.size.toUInt())

        for (import in module.imports) {
            val importDesc = import.description
            val importName = "${import.module}/${import.name}"
            val externValue = imports[importName] ?:
                throw UndefinedImportException(importName, importDesc.type)
            when (externValue.type) {
                ExternalType.FunctionAddress -> {
                    if (importDesc.type != ImportType.Function)
                        throw InvalidImportTypeException(importDesc.type, ExternalType.FunctionAddress, importName)
                    val expectedType = module.types[
                            (importDesc as ImportDescriptionFunction).typeIdx.toInt()]
                    val function = store.getFunction(externValue.address)
                    if (!expectedType.signatureEquals(function.type))
                        throw InvalidImportFunctionTypeException(expectedType, function.type, importName)
                    functions.add(externValue.address)
                }
                ExternalType.TableAddress -> TODO()
                ExternalType.MemoryAddress -> TODO()
                ExternalType.GlobalAddress -> {
                    if (importDesc.type != ImportType.Global)
                        throw InvalidImportTypeException(importDesc.type, ExternalType.GlobalAddress, importName)
                    val expectedType = (importDesc as ImportDescriptionGlobal).value.type
                    val global = store.getGlobal(externValue.address)
                    if (global.value.type != expectedType)
                        throw InvalidImportGlobalTypeException(expectedType, global.value.type, importName)
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

        for (export in module.exports) {
            assert(exports[export.name] == null)
            exports[export.name] = when (export.description.type) {
                ExportType.Function -> ExportInstance(export.name,
                    ExternalValue(ExternalType.FunctionAddress, functions[export.description.idx.toInt()]))
                ExportType.Memory -> ExportInstance(export.name,
                    ExternalValue(ExternalType.MemoryAddress, memories[export.description.idx.toInt()]))
                else -> TODO("Unsupported Export Type ${export.description.type}.")
            }
        }

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
        if (module.start != null) {
            val trap = invoke(functions[module.start.toInt()], stack)
            if (trap != null) throw TrapException(trap)
        }
    }

    fun executeLabel(label: Label, config: Configuration, stack: Stack) {
        for (instr in label)
            // println(instr.descriptor.name)
            instr.execute(config, stack)
        // If this is not true then the label has probably been popped by an Instruction
        if (stack.lastLabel == label)
            stack.popLastLabel()
    }

    fun invoke(export: ExportInstance, params: List<Value>) : InvocationResult {
        if (export.value.type != ExternalType.FunctionAddress)
            throw InvalidExternalTypeException(ExternalType.FunctionAddress, export.value.type)
        // println("Invoking ${export.name}")
        return invoke(export.value.address, params)
    }

    fun invoke(addr: Address, params: List<Value>) : InvocationResult {
        val f = store.functions[addr.toInt()]
        if (params.size != f.type.parameters.size)
            throw IllegalArgumentCountException(addr, f.type.parameters.size.toUInt(), params.size.toUInt())

        for (i in params.indices) {
            val expectedType = f.type.parameters[i]
            if (params[i].type != expectedType)
                throw InvalidArgumentTypeException(addr, i.toUInt(), expectedType, params[i].type)
        }

        val stack = Stack()
        for (v in params) stack.pushValue(v)

        val trap = invoke(addr, stack)
        if (trap != null) return InvocationResult(listOf(), trap)

        return InvocationResult(List(f.type.results.size) {
            val index = f.type.results.size - it - 1
            val expectedType = f.type.results[index]
            val value = stack.popValue()
            if (value.type != expectedType)
                throw InvalidResultTypeException(addr, index.toUInt(), expectedType, value.type)
            value
        }, null)
    }

    fun invoke(addr: Address, stack: Stack) : Trap? {
        val func = store.functions[addr.toInt()]

        val locals = MutableList(func.type.parameters.size) {
            val expectedType = func.type.parameters[it]
            val value = stack.popValue()
            if (value.type != expectedType)
                throw InvalidArgumentTypeException(addr, it.toUInt(), expectedType, value.type)
            value
        }

        return when (func) {
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
                config.thread.trap
            }
            is HostFunctionInstance -> {
                val config = Configuration(store, ComputationThread(Frame(locals, this), listOf()))
                stack.pushFrame(config.thread.frame)
                func.code(config, stack)
                if (stack.lastFrame == config.thread.frame)
                    stack.popLastFrame()
                config.thread.trap
            }
            else -> throw IllegalStateException("Invoking an unsupported Function Instance")
        }
    }
}
