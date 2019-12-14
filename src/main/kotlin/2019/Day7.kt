package `2019`

import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.UnicastSubject
import java.lang.Integer.max
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val input = "3,8,1001,8,10,8,105,1,0,0,21,42,59,76,85,106,187,268,349,430,99999,3,9,102,3,9,9,1001,9,2,9,1002,9,3,9,1001,9,3,9,4,9,99,3,9,102,3,9,9,101,3,9,9,1002,9,2,9,4,9,99,3,9,102,3,9,9,1001,9,4,9,1002,9,5,9,4,9,99,3,9,102,2,9,9,4,9,99,3,9,101,3,9,9,1002,9,2,9,1001,9,4,9,1002,9,2,9,4,9,99,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,1001,9,2,9,4,9,3,9,1001,9,2,9,4,9,3,9,101,2,9,9,4,9,3,9,1002,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,101,1,9,9,4,9,3,9,102,2,9,9,4,9,3,9,101,2,9,9,4,9,99,3,9,1002,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,1001,9,2,9,4,9,3,9,1001,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,1002,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,101,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,101,2,9,9,4,9,99,3,9,1001,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,102,2,9,9,4,9,3,9,101,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,101,2,9,9,4,9,3,9,101,2,9,9,4,9,3,9,1002,9,2,9,4,9,3,9,1001,9,1,9,4,9,3,9,102,2,9,9,4,9,99,3,9,1002,9,2,9,4,9,3,9,1002,9,2,9,4,9,3,9,1001,9,2,9,4,9,3,9,101,1,9,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,101,1,9,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,99,3,9,1002,9,2,9,4,9,3,9,102,2,9,9,4,9,3,9,102,2,9,9,4,9,3,9,1001,9,1,9,4,9,3,9,101,1,9,9,4,9,3,9,1002,9,2,9,4,9,3,9,1001,9,2,9,4,9,3,9,1001,9,2,9,4,9,3,9,102,2,9,9,4,9,3,9,1002,9,2,9,4,9,99"
    val initialRegisters = input.split(',').map(String::toInt)
    var max = 0

    println("Maximum thrust is " + Day7().iterateAmplifiers(initialRegisters, "56789", "", 0))
    exitProcess(0)
}

class Day7() {
    fun iterateAmplifiers(initialRegisters: List<Int>, donor: String, phase: String, currentMax: Int): Int {
        var returnMax = currentMax
        if (donor.isBlank()) {
            val value = runAmplifierWithPhases(phase, initialRegisters)
            return if (value > currentMax) value else currentMax
        } else {
            donor.forEach { current ->
                returnMax = max(returnMax, iterateAmplifiers(initialRegisters, donor.replace(current.toString(), ""), phase + current, currentMax))
            }
        }

        return returnMax
    }

    fun runAmplifierWithPhases(phases: String, initialRegisters: List<Int>): Int {
        val amplifiersCompleteLatch = CountDownLatch(5)
        val amplifiers = mutableListOf<IntCode>()
        var finalResult: Int? = null;

        for (i in 0..4) {
            val registers = mutableListOf<Int>().apply { addAll(initialRegisters) }
            val amplifier = IntCode(registers, "Amplifier" + (i + 1))
            amplifier.completePublishSubject
                .subscribe { amplifiersCompleteLatch.countDown() }
            amplifiers.add(amplifier)
            if (i > 0) {
                amplifiers[i - 1].outputSubject
                    .observeOn(Schedulers.from(amplifiers[i].executor))
                    .subscribe(amplifiers[i].inputSubject)
            }

            amplifier.outputSubject.
                    subscribe { value -> finalResult = value }
        }

        amplifiers[4].outputSubject
            .observeOn(Schedulers.from(amplifiers[0].executor))
            .subscribe(amplifiers[0].inputSubject)

        for (i in 0..4) {
            amplifiers[i].inputSubject.onNext(phases[i].minus('0'))
        }

        amplifiers[0].inputSubject.onNext(0)

        amplifiersCompleteLatch.await()

        return finalResult!!
    }

    class IntCode(private val registers: MutableList<Int>, private val name: String) {
        val executor: Executor = Executors.newSingleThreadExecutor()
        val completePublishSubject = CompletableSubject.create()
        val inputSubject = UnicastSubject.create<Int>()
        val outputSubject = PublishSubject.create<Int>()
        private var programCounter = 0
        private var programComplete = false

        init {
            inputSubject
                .observeOn(Schedulers.from(executor))
                .subscribe { input ->
                    processProgram(input)
                }
        }

        private fun processProgram(input: Int?): Unit {
            var current = input

            while (!programComplete) {
                val opcode = registers[programCounter] % 100
                val params = registers[programCounter]
                val parameterModes = arrayOf(params.div(100) % 2, params.div(1000) % 2, params.div(10000) % 2)
                when (opcode) {
                    1 -> {
                        val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                        val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                        val destination = registers[programCounter + 3]
                        writeValue(registers, destination, arg1 + arg2)
                        programCounter += 4
                    }
                    2 -> {
                        val arg1 = readValue(registers, parameterModes[0], programCounter + 1)
                        val arg2 = readValue(registers, parameterModes[1], programCounter + 2)
                        val destination = registers[programCounter + 3]
                        writeValue(registers, destination, arg1 * arg2)
                        programCounter += 4
                    }
                    3 -> {
                        if (current != null) {
                            val destination = registers[programCounter + 1]
                            writeValue(registers, destination, current.toInt())
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

                    99 -> {
                        programComplete = true
                        completePublishSubject.onComplete()
                        return;
                    }
                }
            }
        }

        private fun readValue(registers: List<Int>, mode: Int, position: Int): Int {
            return if (mode == 0) {
                registers[registers[position]]
            } else {
                registers[position]
            }
        }

        private fun writeValue(registers: MutableList<Int>, position: Int, value: Int) {
            registers[position] = value
        }
    }
}