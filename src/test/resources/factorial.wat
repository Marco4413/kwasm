(module
  (memory $mem (export "memory") 1)
  (func $factorial (export "factorial") (param $n i32) (result f64)
    (block $body (result f64)
      f64.const 1
      ;; Early Return
      local.get $n
      i32.const 1
      i32.le_u
      br_if $body
      (loop $fact_loop (param f64) (result f64)
        local.get $n
        f64.convert_i32_u
        f64.mul
        ;; Increment Iteration
        local.get $n
        i32.const 1
        i32.sub
        local.tee $n
        i32.const 1
        i32.gt_u
        br_if $fact_loop
      )
    )
  )
  (func $gen_factorial (export "gen_factorial") (param $addr i32) (param $count i32)
    (local $i i32)
    (loop $calc_loop
      ;; Calculate Factorial and Store it
      local.get $i
      i32.const 8
      i32.mul
      local.get $addr
      i32.add ;; Address
      local.get $i
      call $factorial
      f64.store
      ;; Increment Iteration
      local.get $i
      i32.const 1
      i32.add
      local.tee $i
      local.get $count
      i32.lt_u
      br_if $calc_loop
    )
  )
)