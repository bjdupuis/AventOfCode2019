package `2019`

import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.UnicastSubject
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class IntCode(private val registers: MutableList<Long>, private val name: String) {
    val executor: Executor = Executors.newSingleThreadExecutor()
    val completePublishSubject = CompletableSubject.create()
    val inputSubject = UnicastSubject.create<Long>()
    val outputSubject = PublishSubject.create<Long>()
    private var programCounter = 0
    private var relativeBase = 0
    private var programComplete = false

    init {
        inputSubject
            .observeOn(Schedulers.from(executor))
            .subscribe { input ->
                processProgram(input)
            }
    }

    private fun processProgram(input: Long?): Unit {
        var current = input

        while (!programComplete) {
            val opcode = registers[programCounter].toInt() % 100
            val params = registers[programCounter].toInt()
            val parameterModes = arrayOf((params % 1000) / 100, (params % 10000) / 1000, params / 10000)
            when (opcode) {
                1 -> {
                    val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                    val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                    val destination = registers[programCounter + 3].toInt()

                    writeValue(registers, parameterModes[2], destination, arg1 + arg2)
                    programCounter += 4
                }
                2 -> {
                    val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                    val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                    val destination = registers[programCounter + 3].toInt()

                    writeValue(registers, parameterModes[2], destination, arg1 * arg2)
                    programCounter += 4
                }
                3 -> {
                    if (current != null) {
                        val destination = registers[programCounter + 1].toInt()

                        writeValue(registers, parameterModes[0], destination, current.toLong())
                        current = null;
                        programCounter += 2
                    } else {
                        return;
                    }
                }
                4 -> {
                    val arg = readValue(registers, parameterModes[0], programCounter + 1)
                    outputSubject.onNext(arg)
                    programCounter += 2
                }
                5 -> {
                    val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                    val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                    if (arg1 != 0L) {
                        programCounter = arg2.toInt()
                    } else {
                        programCounter += 3
                    }
                }
                6 -> {
                    val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                    val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                    if (arg1 == 0L) {
                        programCounter = arg2.toInt()
                    } else {
                        programCounter += 3
                    }
                }
                7 -> {
                    val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                    val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                    val destination = registers[programCounter + 3].toInt()

                    writeValue(registers, parameterModes[2], destination, if (arg1 < arg2) 1 else 0)

                    programCounter += 4
                }
                8 -> {
                    val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                    val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                    val destination = registers[programCounter + 3].toInt()

                    writeValue(registers, parameterModes[2], destination, if (arg1 == arg2) 1 else 0)

                    programCounter += 4
                }
                9 -> {
                    val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                    relativeBase += arg1.toInt()
                    programCounter += 2
                }

                99 -> {
                    programComplete = true
                    completePublishSubject.onComplete()
                    return;
                }
                else -> {
                    println("Illegal opcode: $opcode")
                    exitProcess(-1)
                }
            }
        }
    }

    private fun readValue(registers: MutableList<Long>, mode: Int, position: Int): Long {
        val accessPosition = when (mode) {
            0 -> registers[position].toInt()
            1 -> position
            2 -> registers[position].toInt() + relativeBase
            else -> {
                println("Invalid access mode $mode")
                exitProcess(-1)
                0
            }
        }

        resizeRegistersIfNeeded(accessPosition, registers)

        return registers[accessPosition]
    }

    private fun writeValue(registers: MutableList<Long>, mode: Int, position: Int, value: Long) {
        val accessPosition = when(mode) {
            0 -> position
            1 -> {
                println("Immediate mode on write is not legal")
                exitProcess(-1)
            }
            2 -> position + relativeBase
            else -> {
                println("Illegal access mode on write: $mode")
                exitProcess(-1)
            }
        }
        resizeRegistersIfNeeded(accessPosition, registers)
        registers[accessPosition] = value
    }

    private fun resizeRegistersIfNeeded(accessPosition: Int, registers: MutableList<Long>) {
        if (accessPosition >= registers.size) {
            for (newPosition in registers.size..accessPosition) {
                registers.add(0L)
            }
        }
    }
}
