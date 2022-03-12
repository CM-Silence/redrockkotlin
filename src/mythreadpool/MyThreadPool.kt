package mythreadpool

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger


class MyThreadPool(private var curSize : AtomicInteger, //核心线程数
                   private val maxCurSize :Int, //最大线程数
                   private val keepAliveTime : Long, //最大空闲时间
                   @Volatile private var taskQueue : BlockingQueue<Runnable>, //并发任务队列,控制并发拉取
                   private val handler : RejectHandler = MyRejectHandler()) //拒绝策略
{

    @Volatile private var isShutDown : Boolean = false //线程池是否已关闭

    private var freeCorePool : ConcurrentLinkedQueue<CoreThread> = ConcurrentLinkedQueue() //空闲核心线程队列
    private var freeNonCorePool : ConcurrentLinkedQueue<NonCoreThread> = ConcurrentLinkedQueue() //空闲非核心线程队列

    private var taskQueueSize : AtomicInteger = AtomicInteger(0) //当前待执行任务队列大小
    private val taskQueueCapacity : Int = 10 //任务队列容量(最多接取的任务数量)

    var rejectTaskCount : AtomicInteger = AtomicInteger(0) //拒绝的任务数量


    //初始化空闲核心线程
    init {
        val core = CoreThread()
        core.name = "core"
        freeCorePool.add(core)

    }

    companion object {

        @JvmStatic
        fun newSingleThreadExecutor() : MyThreadPool{
            return MyThreadPool(AtomicInteger(1), 1,0L,LinkedBlockingDeque())

        }

        @JvmStatic
        fun newFixedThreadPool(poolSize : Int) : MyThreadPool{
            return MyThreadPool(AtomicInteger(1), poolSize,10 * 1000L,LinkedBlockingDeque())

        }

        /*暂时没实现
        @JvmStatic
        fun newScheduledThreadPool(corePoolSize : AtomicInteger) : MyThreadPool{
            return MyThreadPool(corePoolSize, Int.MAX_VALUE,60 * 1000L,SynchronousQueue())

        }
        */

        @JvmStatic
        fun newCachedThreadPool() : MyThreadPool {
            return MyThreadPool(AtomicInteger(1), Int.MAX_VALUE,60 * 1000L,LinkedBlockingDeque())
        }

    }

    /**
     * @param task 任务
     * @description: 有以下几种情况：
     * 1.如果线程池已经关闭则直接拒绝任务
     * 2.成功交付空闲线程
     * 3.成功加任务队列
     * 4.如果队列已满且已达最大线程数，那么拒绝任务
     * @return: void
     */
    fun execute(task : Runnable) {
        if (isShutDown) {   //如果线程池已经关闭
            reject(task,"线程池已关闭") //拒绝任务
        }
        //如果无法从核心线程池拉取线程后也无法从非核心线程池拉取线程后且任务容量已满
        else if (!executeByCore(task) && !executeByNonCore(task) && !waitInTaskQueue(task)) {
            if (curSize.get() == maxCurSize) { //如果达到了最大线程数
                reject(task,"线程数达到上限") //拒绝任务
            }
            else if (curSize.incrementAndGet() <= maxCurSize) { //如果没达到最大线程数
                val nonCore = NonCoreThread() //新建非核心线程
                nonCore.name = "nonCore" + (curSize.get() - 2) //设置线程名称
                freeNonCorePool.add(nonCore) //将线程加入空闲非核心队列中
                executeByNonCore(task) //拉取非核心线程执行任务
            }
            else { //添加任务失败
                curSize.decrementAndGet() //线程数-1(因为上面将线程数+1进行了判断)
                reject(task,"未知原因") //拒绝任务
            }
        }
    }

    /**
     * @param task
     * @description: 尝试从空闲的核心线程队列中拉取一个线程来执行任务
     * @return: boolean true为拉取成功，false为拉取失败
     */
    private fun executeByCore(task: Runnable) : Boolean{
        val core : CoreThread? = freeCorePool.poll() //尝试从空闲核心线程池拉取一个线程
        if(core != null){ //如果拉取到了线程
            core.setTask(task) //设置线程的任务
            if (core.state == Thread.State.NEW) {
                core.start() //启动线程
            }
            return true
        }
        return false
    }

    /**
     * @param task
     * @description: 尝试从空闲的非核心线程队列中拉取一个线程来执行任务
     * @return: boolean true为拉取成功，false为拉取失败
     */
    private fun executeByNonCore(task : Runnable) : Boolean{
        val nonCore : NonCoreThread? = freeNonCorePool.poll() //尝试从空闲非核心线程池拉取一个线程
        return if (nonCore != null) { //如果拉取到了线程
            nonCore.setTask(task) //设置线程的任务
            if (nonCore.state == Thread.State.NEW) {
                nonCore.start() //启动线程
            }
            true
        } else false
    }

    /**
     * @param task
     * @description: 尝试把任务加入队列，用原子变量控制
     * @return: boolean true为添加成功，false为添加失败
     */
    private fun waitInTaskQueue(task : Runnable) : Boolean{
        //如果待执行任务队列大小+1后仍在待执行任务队列的容量内,则添加成功
        return if (taskQueueSize.incrementAndGet() <= taskQueueCapacity) {
            taskQueue.add(task) //将任务加入待执行任务队列中
            println("加入队列任务——$task")
            true
        }
        //+1后超出容量,则添加失败,需要把待执行任务队列大小-1(因为刚开始的时候+1了)
        else {
            taskQueueSize.decrementAndGet() //待执行任务队列大小-1
            false
        }
    }

    //拒绝当前任务
    private fun reject(task : Runnable,reason : String){
        handler.setReason(reason)
        handler.reject(task)
        this.rejectTaskCount.incrementAndGet()
    }

    //关闭线程池
    fun setIsShutDown(isShutDown : Boolean){
        this.isShutDown = isShutDown
    }

    /**
     * @description: 核心线程不断工作，完成当前任务后会尝试从待执行任务队列中拉取任务，在shutDown前不会消亡
     */
    inner class CoreThread : Thread() {
        @Volatile private var isFree : Boolean = false //是否空闲
        @Volatile private var task : Runnable? = null //任务

        //拉取任务
        private fun pullTask(){
            val task : Runnable? = taskQueue.poll() //从待执行任务队列中拉取任务
            if(task != null){ //如果拉取到了任务
                setTask(task) //设置线程的任务
                taskQueueSize.decrementAndGet() //待执行任务队列大小-1
                freeCorePool.remove(this) //移出空闲核心线程队列
            }
            else{ //如果没拉取到任务
                isFree = true //将核心线程设为空闲状态
                freeCorePool.add(this) //加入空闲核心线程队列
            }
        }

        //设置线程的任务
        fun setTask(task : Runnable){
            isFree = false //线程有任务了,将其设为非空闲状态
            this.task = task //设置线程的任务
        }

        override fun run() {
            //当线程池没有关闭或任务队列没空前反复执行
            while (!isShutDown || !taskQueue.isEmpty()) {
                if (task != null) { //如果任务不为空则执行任务
                    try {
                        task?.run() //尝试执行任务
                        println(currentThread().name + "完成任务——" + task)
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                    task = null //任务完成后将线程的task属性设值为null
                }
                //如果核心线程不空闲且没有任务(task为null)则反复尝试拉取任务
                if(!isFree && task == null)
                    pullTask()  //二者都用volatile遵循happens-before,防止主线程修改到一半就去拉取
            }
            println("${currentThread().name}消亡")
        }
    }

    /**
     * @description: 非核心线程不断工作，完成当前任务后会尝试从待执行任务队列中拉取任务，在空闲一定时间后消亡
     */
    inner class NonCoreThread : Thread() {
        @Volatile private var isFree : Boolean = false //是否空闲
        private var task : Runnable? = null //任务
        private var beginFree : Long = 0 //空闲状态持续的时间

        //拉取任务
        private fun pullTask(){
            val task : Runnable? = taskQueue.poll() //从待执行任务队列中拉取任务
            if(task != null){ //如果任务不为空
                setTask(task) //设置该线程的任务
                taskQueueSize.decrementAndGet() //将待执行任务队列大小-1
                freeNonCorePool.remove(this) //移出空闲核心线程队列
            }
            else{ //如果任务为空
                isFree = true //将线程设为空闲
                beginFree = System.currentTimeMillis() //设置空闲时间
                freeNonCorePool.add(this) //加入空闲非核心线程队列
            }
        }

        //设置任务
        fun setTask(task : Runnable){
            isFree = false //线程有任务则设为不空闲状态
            this.task = task //设置线程的任务
        }

        override fun run() {
            //当(线程池没关闭或任务队列不为空)且(线程不空闲或空闲时间没超过最大空闲时间)时反复执行
            while ((!isShutDown || !taskQueue.isEmpty()) && (!isFree || System.currentTimeMillis() - beginFree < keepAliveTime)) {
                if (task != null) { //如果任务不为空
                    try {
                        task?.run() //尝试执行任务
                        println(currentThread().name + "完成任务——" + task)
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                    task = null //完成任务后将线程的task属性设为空
                }
                if(!isFree&&task==null) //如果线程不空闲且线程的任务为空
                    pullTask() //反复尝试拉取任务
            }
            //当线程不符合while语句中的条件后
            curSize.decrementAndGet() //线程数-1
            freeNonCorePool.remove(this)//把消亡的线程从队列中移除,消亡后的线程不为null,不会被上面的executeByNonCore感知到,进而造成任务丢失
            println(currentThread().name + "消亡")

        }
    }

}