package linkedlistblockingqueue

import java.util.Scanner

fun main() {
    val linkedListBlockingQueue : MyLinkedBlockingQueue<String> = MyLinkedBlockingQueue(20)
    val putTime = 500L
    val takeTime = 1000L
    Thread{
        var i = 0
        while (true){
            Thread.sleep(putTime)
            linkedListBlockingQueue.put("thread0.put--->$i")
            println("thread0.put--->$i")
            i++
        }
    }.start()

    Thread{
        var i = 0
        while (true){
            Thread.sleep(putTime)
            linkedListBlockingQueue.put("thread1.put--->$i")
            println("thread1.put--->$i")
            i++
        }
    }.start()

    Thread{
        while (true){
            Thread.sleep(takeTime)
            println("thread2.take--->${linkedListBlockingQueue.take()}")
        }
    }.start()

    Thread{
        while (true){
            Thread.sleep(takeTime)
            println("thread3.take--->${linkedListBlockingQueue.take()}")
        }
    }.start()

    println("输入任意内容退出程序")
    val i = Scanner(System.`in`).next() //拦住主线程
}