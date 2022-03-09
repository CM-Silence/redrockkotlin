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

    fun put(item : T){
        lock.lock() //先锁上
        try {
            while (num == max){ //如果容量满了就堵塞线程
                try {
                    notFull.await() //如果容量满了就锁上notFull来堵塞线程
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            linkedList.add(item)
            num++
            notEmpty.signal() //如果put方法成功将一个元素放进集合中的话说明集合不为空,则解锁take方法中的notEmpty
        }finally {
            lock.unlock() //解锁
        }
    }

    fun take() : T{
        lock.lock() //先锁上
        try {
            while (num == 0){ //如果集合空了就堵塞线程
                try {
                    notEmpty.await() //如果集合空了就锁上notEmpty来堵塞线程
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            val item : T = linkedList.removeFirst()
            num--
            notFull.signal() //如果take方法成功拿出一个元素的话说明集合没满,则解锁put方法中的notFull
            return item
        }finally {
            lock.unlock() //解锁
        }
    }

}
