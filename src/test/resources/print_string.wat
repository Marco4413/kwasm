(module
  (import "env" "c_print" (func $c_print (param i32)))
  (memory (export "memory") 1)
  (data (i32.const 0) "Hello World!\00")
  (func $print (export "print") (param $addr i32)
    (local $char i32)
    (loop $print_loop
      block $print_char
        ;; Checking if character is '\0'
        local.get $addr
        i32.load8_u
        local.tee $char
        i32.eqz
        br_if $print_char
        ;; Printing character
        local.get $char
        call $c_print
        ;; Increasing address
        local.get $addr
        i32.const 1
        i32.add
        local.set $addr
        br $print_loop
      end $print_char
    )
  )
)