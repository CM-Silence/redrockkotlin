package mythreadpool

import java.util.Scanner
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    val myThreadPool = MyThreadPool.newFixedThreadPool(AtomicInteger(5))
    Thread{
        repeat(10){
        myThreadPool.execute(MyTask())
        }
    }.start()

    Thread{
        repeat(10){
            Thread.sleep(1500L)
            myThreadPool.execute(MyTask())
        }
    }.start()

    Thread{
        repeat(10){
            Thread.sleep(2500L)
            myThreadPool.execute(MyTask())
        }
    }.start()

    val i = Scanner(System.`in`).next()
}