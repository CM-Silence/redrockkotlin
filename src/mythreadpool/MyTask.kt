package mythreadpool

import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

//自定义任务类
class MyTask(private val num: Int = sum.getAndIncrement()) : Runnable {  //将任务数量+1并设置任务序号

    companion object{
        @JvmStatic private var sum : AtomicInteger = AtomicInteger(0) //任务数量
    }


    override fun run() {
        try {
            if(num % 5 == 1){
                throw Exception("${Thread.currentThread().name}执行任务——${num}失败!") //模拟报错
            }
            println("${Thread.currentThread().name}准备执行任务——$num")
            Thread.sleep(Random.nextLong(500L,5000L))
        } catch (e :InterruptedException) {
            e.printStackTrace()
        }
    }


    override fun toString() : String{
        return num.toString()
    }

}