package linkedlistblockingqueue

import java.util.LinkedList
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

//work 1
//LinkedList封装实现阻塞队列功能
class MyLinkedBlockingQueue<T>(private val max : Int) {

    constructor() : this(Int.MAX_VALUE) //无参次构造函数,调用主构造函数将集合容量设为最大值

    private val linkedList : LinkedList<T> = LinkedList() //储存数据的集合

    private val lock : ReentrantLock = ReentrantLock() //锁

    //Condition是ReentrantLock类的重要接口
    private val notFull : Condition = lock.newCondition()
    private val notEmpty : Condition = lock.newCondition()

    private var num : Int = 0 //当前集合中的元素个数

    public fun put(item : T){
        lock.lock() //先锁上
        try {
            while (num == max){ //如果容量满了就堵塞线程
                try {
                    notFull.await()
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            linkedList.add(item)
            num++
            notEmpty.signal()
        }finally {
            lock.unlock() //解锁
        }
    }

    public fun take() : T{
        lock.lock() //先锁上
        try {
            while (num == 0){ //如果容量空了就堵塞线程
                try {
                    notEmpty.await()
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            val item : T = linkedList.removeFirst()
            num--
            notFull.signal()
            return item
        }finally {
            lock.unlock() //解锁
        }
    }

}
