package mythreadpool

import java.util.Scanner

fun main() {
    println("请选择线程池:\n1.SingleThreadExecutor\n2.FixedThreadPool(容量为5)\n3.ScheduledThreadPool(容量为5)\n4.CachedThreadPool\n输入其他内容则默认为SingleThreadExecutor")
    val myThreadPool : MyThreadPool =
        when(Scanner(System.`in`).next()){
        "1" ->{
            MyThreadPool.newSingleThreadExecutor()
        }
        "2" ->{
            MyThreadPool.newFixedThreadPool(5)
        }
        /*
        "3" ->{
            MyThreadPool.newScheduledThreadPool(AtomicInteger(5))
        }
        */
        "4" ->{
            MyThreadPool.newCachedThreadPool()
        }

        else ->{
            MyThreadPool.newSingleThreadExecutor()
        }
    }
    Thread{
        repeat(20){
            myThreadPool.execute(MyTask())
        }
    }.start()

    Thread{
        repeat(15){
            Thread.sleep(500L)
            myThreadPool.execute(MyTask())
        }
    }.start()

    Thread{
        repeat(15){
            Thread.sleep(1500L)
            myThreadPool.execute(MyTask())
        }
    }.start()

    Thread{
        Thread.sleep(15000L)
        myThreadPool.setIsShutDown(true) //一定时间后关闭线程池
    }.start()
}