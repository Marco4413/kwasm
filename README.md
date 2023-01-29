# kwasm

A Wasm parser and interpreter written in Kotlin.

**I'd also like to specify that I'm using this project to learn Kotlin.
So things may not be written in the best way possible.**

## References

https://webassembly.github.io/spec/core/index.html  
https://webassembly.github.io/spec/core/binary/index.html  
https://webassembly.github.io/spec/core/exec/index.html  
https://en.wikipedia.org/wiki/LEB128

https://github.com/WebAssembly/wabt  
https://webassembly.github.io/wabt/demo/wat2wasm  
https://webassembly.github.io/wabt/demo/wasm2wat

## What can this do right now?

This project is now able to parse basic Wasm and run it.
Elements, Tables and 'partially' Globals are not yet supported.

I plan on adding support for Wat parsing (or a custom language) in the future.

Here's a rough list of the supported features:
- [ ] Binary Format (read/write)
  - [x] Values
  - [ ] Types
    - [x] Value Types
    - [x] Function Types
    - [x] Limits
    - [x] Memory Types
    - [ ] Table Types
    - [x] Global Types
  - [ ] Instructions
    - [ ] Control (75%)
    - [ ] Reference
    - [ ] Parametric (33%)
    - [ ] Variable
      - [x] Local
      - [ ] Global (50%)
    - [ ] Table
    - [ ] Memory (~10%)
    - [ ] Numeric
      - [ ] I32 (20%)
      - [ ] I64
      - [ ] F32
      - [ ] F64
    - [ ] Vector
  - [ ] Modules
    - [x] Magic
    - [x] Version
    - [ ] Sections
      - [x] Custom
      - [x] Type
      - [ ] Import
        - [x] Function
        - [ ] Table
        - [ ] Memory
        - [x] Global
      - [x] Function
      - [ ] Table
      - [x] Memory
      - [ ] Global
      - [x] Export
      - [x] Start
      - [ ] Element
      - [x] Code
      - [x] Data
      - [ ] Data Count
- [ ] ~~Validation~~ Who needs validation?
- [ ] Execution
  - [ ] Runtime Structures
    - [ ] Values
      - [x] Number
      - [ ] Vector
      - [ ] Reference
    - [ ] Store
      - [x] Functions
      - [ ] Tables
      - [x] Memories
      - [x] Globals
      - [ ] Elements
      - [x] Data
    - [ ] Instances
      - [ ] Module
        - [x] Types
        - [x] Functions
        - [ ] Tables
        - [x] Memories
        - [ ] Globals (only from Imports)
        - [ ] Elements
        - [x] Data
        - [x] Exports
      - [x] Function
      - [ ] Table
      - [x] Memory
      - [x] Global
      - [ ] Element
      - [x] Data
      - [x] Export
    - [ ] External Values
      - [x] Function
      - [ ] Table
      - [x] Memory
      - [x] Global
    - [x] Stack
    - [x] Label

These categories were taken from https://webassembly.github.io/spec/core/index.html

I'd need some Wasm examples which use the features that are not yet implemented to undestand exactly how they work
and test them.

## Examples

Usage examples can be found in the [test](src/test) Kotlin module.
