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
Memories, Tables and Globals are not yet supported (as well as other 'complex stuff').

I plan on adding support for Wat parsing (or a custom language) in the future.
