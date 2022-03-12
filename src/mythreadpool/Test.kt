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
        repeat(30){
            myThreadPool.execute(MyTask()) //瞬间添加
        }
    }.start()

    Thread{
        repeat(20){
            Thread.sleep(500L)
            myThreadPool.execute(MyTask()) //每0.5秒添加1次,在第10秒时全部添加完成
        }
    }.start()

    Thread{
        repeat(20){
            Thread.sleep(1000L)
            myThreadPool.execute(MyTask()) //每1秒添加1次,在第20秒时全部添加完成
        }
    }.start()

    Thread{
        repeat(15){
            Thread.sleep(2000L)
            myThreadPool.execute(MyTask()) //每2秒添加1次,在第30秒时全部添加完成
        }
        println("拒绝的任务总数为:${myThreadPool.rejectTaskCount}")
    }.start()

    Thread{
        Thread.sleep(25000L)
        myThreadPool.setIsShutDown(true) //一定时间后关闭线程池
    }.start()
}