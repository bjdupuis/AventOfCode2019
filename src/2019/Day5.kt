package `2019`

fun main(args: Array<String>) {
    val input = "3,225,1,225,6,6,1100,1,238,225,104,0,1102,68,5,225,1101,71,12,225,1,117,166,224,1001,224,-100,224,4,224,102,8,223,223,101,2,224,224,1,223,224,223,1001,66,36,224,101,-87,224,224,4,224,102,8,223,223,101,2,224,224,1,223,224,223,1101,26,51,225,1102,11,61,224,1001,224,-671,224,4,224,1002,223,8,223,1001,224,5,224,1,223,224,223,1101,59,77,224,101,-136,224,224,4,224,1002,223,8,223,1001,224,1,224,1,223,224,223,1101,11,36,225,1102,31,16,225,102,24,217,224,1001,224,-1656,224,4,224,102,8,223,223,1001,224,1,224,1,224,223,223,101,60,169,224,1001,224,-147,224,4,224,102,8,223,223,101,2,224,224,1,223,224,223,1102,38,69,225,1101,87,42,225,2,17,14,224,101,-355,224,224,4,224,102,8,223,223,1001,224,2,224,1,224,223,223,1002,113,89,224,101,-979,224,224,4,224,1002,223,8,223,1001,224,7,224,1,224,223,223,1102,69,59,225,4,223,99,0,0,0,677,0,0,0,0,0,0,0,0,0,0,0,1105,0,99999,1105,227,247,1105,1,99999,1005,227,99999,1005,0,256,1105,1,99999,1106,227,99999,1106,0,265,1105,1,99999,1006,0,99999,1006,227,274,1105,1,99999,1105,1,280,1105,1,99999,1,225,225,225,1101,294,0,0,105,1,0,1105,1,99999,1106,0,300,1105,1,99999,1,225,225,225,1101,314,0,0,106,0,0,1105,1,99999,7,677,677,224,1002,223,2,223,1006,224,329,1001,223,1,223,1007,226,226,224,1002,223,2,223,1006,224,344,1001,223,1,223,1108,226,677,224,102,2,223,223,1005,224,359,1001,223,1,223,1107,226,677,224,1002,223,2,223,1006,224,374,101,1,223,223,1107,677,226,224,1002,223,2,223,1006,224,389,101,1,223,223,7,226,677,224,1002,223,2,223,1005,224,404,101,1,223,223,1008,677,226,224,102,2,223,223,1005,224,419,101,1,223,223,1008,226,226,224,102,2,223,223,1006,224,434,101,1,223,223,107,226,226,224,1002,223,2,223,1005,224,449,1001,223,1,223,108,226,677,224,102,2,223,223,1005,224,464,101,1,223,223,1108,677,226,224,102,2,223,223,1005,224,479,101,1,223,223,1007,226,677,224,102,2,223,223,1006,224,494,101,1,223,223,107,677,677,224,102,2,223,223,1005,224,509,101,1,223,223,108,677,677,224,102,2,223,223,1006,224,524,1001,223,1,223,8,226,677,224,102,2,223,223,1005,224,539,101,1,223,223,107,677,226,224,102,2,223,223,1005,224,554,1001,223,1,223,8,226,226,224,102,2,223,223,1006,224,569,1001,223,1,223,7,677,226,224,1002,223,2,223,1005,224,584,1001,223,1,223,1108,226,226,224,102,2,223,223,1005,224,599,1001,223,1,223,1107,677,677,224,1002,223,2,223,1006,224,614,1001,223,1,223,1007,677,677,224,1002,223,2,223,1006,224,629,1001,223,1,223,108,226,226,224,102,2,223,223,1005,224,644,1001,223,1,223,8,677,226,224,1002,223,2,223,1005,224,659,1001,223,1,223,1008,677,677,224,1002,223,2,223,1006,224,674,1001,223,1,223,4,223,99,226"
    val initialRegisters = input.split(',').map(String::toInt)

    val registers = mutableListOf<Int>().apply {addAll(initialRegisters)}
    processProgram(registers)
}

fun processProgram(registers: MutableList<Int>): MutableList<Int> {
    var programCounter = 0
    var programComplete = false

    while (!programComplete) {
        val opcode = registers[programCounter] % 100
        val params = registers[programCounter]
        val parameterModes = arrayOf(params.div(100) % 2, params.div(1000) % 2, params.div(10000) % 2)
        when (opcode) {
            1 -> {
                val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                val destination = registers[programCounter + 3]
                writeValue(registers, destination,arg1 + arg2)
                programCounter += 4
            }
            2 -> {
                val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                val destination = registers[programCounter + 3]
                writeValue(registers, destination,arg1 * arg2)
                programCounter += 4
            }
            3 -> {
                val destination = registers[programCounter + 1]
                var input: String?
                do {
                    println("Need input (non-null): ")
                    input = readLine()
                } while (input == null)
                writeValue(registers, destination, input.toInt())
                programCounter += 2
            }
            4 -> {
                val arg = readValue(registers, parameterModes[0], programCounter + 1)
                println("Output is $arg")
                programCounter += 2
            }
            5 -> {
                val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                if (arg1 != 0) {
                    programCounter = arg2
                } else {
                    programCounter += 3
                }
            }
            6 -> {
                val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                if (arg1 == 0) {
                    programCounter = arg2
                } else {
                    programCounter += 3
                }
            }
            7 -> {
                val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                val destination = registers[programCounter + 3]
                writeValue(registers, destination, if (arg1 < arg2) 1 else 0)

                programCounter += 4
            }
            8 -> {
                val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                val destination = registers[programCounter + 3]
                writeValue(registers, destination, if (arg1 == arg2) 1 else 0)

                programCounter += 4
            }

            99 -> programComplete = true
        }
    }

    return registers
}

fun readValue(registers: List<Int>, mode: Int, position: Int): Int {
    return if (mode == 0) {
        registers[registers[position]]
    } else {
        registers[position]
    }
}

fun writeValue(registers: MutableList<Int>, position: Int, value: Int) {
    registers[position] = value
}