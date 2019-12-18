package `2019`

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.intellij.lang.annotations.Flow
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    println("Number of regions affect is "+Day19().scanRegion(IntRange(0, 50), IntRange(0, 50)))

    exitProcess(0)
}

class Day19 {
    private val initialRegisters = "109,424,203,1,21102,1,11,0,1106,0,282,21101,18,0,0,1105,1,259,2101,0,1,221,203,1,21101,0,31,0,1105,1,282,21101,38,0,0,1105,1,259,21001,23,0,2,22101,0,1,3,21102,1,1,1,21101,57,0,0,1106,0,303,2102,1,1,222,20102,1,221,3,20102,1,221,2,21101,0,259,1,21102,80,1,0,1105,1,225,21102,1,130,2,21102,1,91,0,1106,0,303,2101,0,1,223,21002,222,1,4,21102,259,1,3,21102,1,225,2,21101,0,225,1,21102,1,118,0,1106,0,225,21002,222,1,3,21101,0,106,2,21102,1,133,0,1106,0,303,21202,1,-1,1,22001,223,1,1,21101,148,0,0,1105,1,259,2102,1,1,223,20101,0,221,4,20102,1,222,3,21102,1,19,2,1001,132,-2,224,1002,224,2,224,1001,224,3,224,1002,132,-1,132,1,224,132,224,21001,224,1,1,21101,195,0,0,106,0,109,20207,1,223,2,20101,0,23,1,21102,-1,1,3,21101,0,214,0,1105,1,303,22101,1,1,1,204,1,99,0,0,0,0,109,5,1201,-4,0,249,21201,-3,0,1,21202,-2,1,2,21201,-1,0,3,21102,1,250,0,1105,1,225,22102,1,1,-4,109,-5,2106,0,0,109,3,22107,0,-2,-1,21202,-1,2,-1,21201,-1,-1,-1,22202,-1,-2,-2,109,-3,2106,0,0,109,3,21207,-2,0,-1,1206,-1,294,104,0,99,21201,-2,0,-2,109,-3,2105,1,0,109,5,22207,-3,-4,-1,1206,-1,346,22201,-4,-3,-4,21202,-3,-1,-1,22201,-4,-1,2,21202,2,-1,-1,22201,-4,-1,1,22102,1,-2,3,21102,343,1,0,1105,1,303,1105,1,415,22207,-2,-3,-1,1206,-1,387,22201,-3,-2,-3,21202,-2,-1,-1,22201,-3,-1,3,21202,3,-1,-1,22201,-3,-1,2,21201,-4,0,1,21101,384,0,0,1106,0,303,1106,0,415,21202,-4,-1,-4,22201,-4,-3,-4,22202,-3,-2,-2,22202,-2,-4,-4,22202,-3,-2,-3,21202,-4,-1,-2,22201,-3,-2,1,21201,1,0,-4,109,-5,2106,0,0"
        .split(",")
        .map(String::toLong)
    private val brain = IntCode(mutableListOf<Long>().apply { addAll(initialRegisters) }, "Brain")

    fun scanRegion(xRange: IntRange, yRange: IntRange): Int {
        val countDownLatch = CountDownLatch(1)
        var affectedRegionCount = 0
        val compositeDisposable = CompositeDisposable()

        compositeDisposable.add(brain.completePublishSubject.subscribe {
            brain.reset()
        })

        compositeDisposable.add(brain.outputSubject.subscribe {
            when (it) {
                1L -> affectedRegionCount++
            }
        })

        compositeDisposable.add(Flowable.range(xRange.first, xRange.last)
            .flatMap { x ->
                Flowable.range(yRange.first, yRange.last)
                    .map {
                        Pair(x, it)
                    }
            }
            .flatMap { pair ->
                Flowable.fromArray(pair.first, pair.second)
            }
            .zipWith(brain.awaitingInputSubject
                .toFlowable(BackpressureStrategy.BUFFER),
                BiFunction { value: Int, _:Unit -> value }, false, 1 )
            .doOnComplete { countDownLatch.countDown() }
            .subscribe {
                brain.inputSubject.onNext(it.toLong())
            })

        countDownLatch.await()
        compositeDisposable.clear()
        return affectedRegionCount
    }
}