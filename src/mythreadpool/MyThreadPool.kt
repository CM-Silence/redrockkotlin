package mythreadpool

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger


class MyThreadPool(val corePoolSize : Int, val maximumPoolSize :Int, val keepAliveTime : Long, val unit : TimeUnit,
                   val workQueue : BlockingQueue<Runnable>, val threadFactory : ThreadFactory = Executors.defaultThreadFactory(), private val handler : RejectHandler = MyRejectHandler()) {

    @Volatile private var isShutDown : Boolean = false
    @Volatile private var taskQueue : ConcurrentLinkedQueue<Runnable> = ConcurrentLinkedQueue()
    private var freeCorePool : ConcurrentLinkedQueue<CoreTask> = ConcurrentLinkedQueue()
    private var freeNonCorePool : ConcurrentLinkedQueue<NonCoreTask> = ConcurrentLinkedQueue()
    private var queueSize : AtomicInteger = AtomicInteger(0)
    private var curSize : AtomicInteger = AtomicInteger(2)

    //初始化空闲核心线程
    init {
        val core0 : CoreTask = CoreTask()
        core0.name = "core0"
        val core1 : CoreTask = CoreTask()
        core1.name = "core1"
        freeCorePool.add(core0)
        freeCorePool.add(core1)
    }

    companion object {

        @JvmStatic
        fun newSingleThreadExecutor() : MyThreadPool{
            return MyThreadPool(1, 1,0L,TimeUnit.MILLISECONDS,LinkedBlockingDeque<Runnable>())

        }

        @JvmStatic
        fun newFixedThreadPool(poolSize : Int) : MyThreadPool{
            return MyThreadPool(poolSize, poolSize,0L,TimeUnit.MILLISECONDS,LinkedBlockingDeque<Runnable>())

        }

        @JvmStatic
        fun newScheduledThreadPool(corePoolSize : Int) : MyThreadPool{
            return MyThreadPool(corePoolSize, Int.MAX_VALUE,60L,TimeUnit.SECONDS,SynchronousQueue<Runnable>())

        }

        @JvmStatic
        fun newCachedThreadPool() : MyThreadPool {
            return MyThreadPool(0, Int.MAX_VALUE,60L,TimeUnit.SECONDS,SynchronousQueue<Runnable>())
        }
    }

    public fun execute(task : Runnable){
        if(isShutDown) handler.reject(task)

    }

    private fun executeByCore(task: Runnable) : Boolean{

    }

    /**
     * @description: 核心线程不断工作，完成当前任务后会尝试从队列中拉取任务，在shutDown前不会消亡
     */
    inner class CoreTask : Thread() {
        @Volatile private var isFree : Boolean = false
        @Volatile private lateinit var task : Runnable

        public fun pullTask(){
            val task : Runnable = taskQueue.poll()
            if(task != null){
                setTask(task)
                queueSize.decrementAndGet()
            }
            else{
                isFree = true
                freeCorePool.add(this)
            }
        }

        private fun setTask(task : Runnable){
            isFree = false
            this.task = task
        }

        override fun run() {
            while (!isShutDown || !taskQueue.isEmpty()) {
                if (task != null) {
                    task.run()
                    println(currentThread().name + "完成任务——" + task)
                    task = null
                }
                //这个判断条件是很苛刻的
                if(!isFree&&task==null)
                    pullTask()//二者都用volatile遵循happens-bofore,防止主线程修改到一半就去拉取
            }
            println("${currentThread().name}消亡")
        }
    }

    /**
     * @description: 非核心线程不断工作，完成当前任务后会尝试从队列中拉取任务，在空闲一定时间后消亡
     */
    inner class NonCoreTask : Thread() {
        @Volatile private var isFree : Boolean = false
        private lateinit var task : Runnable
        private var beginFree : Long = 0

        public fun pullTask(){
            val task : Runnable = taskQueue.poll()
            if(task != null){
                setTask(task)
                queueSize.decrementAndGet()
            }
            else{
                isFree = true
                freeNonCorePool.add(this)
            }
        }

        private fun setTask(task : Runnable){
            isFree = false
            this.task = task
        }

        override fun run() {
            while ((!isShutDown || !taskQueue.isEmpty()) && (!isFree || System.currentTimeMillis() - beginFree < keepAlive)) {
                if (task != null) {
                    task.run();
                    System.out.println(Thread.currentThread().getName() + "完成任务——" + task);
                    task = null;
                }
                if(!isFree&&task==null) pullTask();
            }
            curSize.decrementAndGet();
            freeNonCorePool.remove(this);//把消亡的线程从队列中移除，消亡后的线程不为null，不会被上面的executrByNonCore感知到，进而造成任务丢失
            println(Thread.currentThread().getName() + "消亡");

        }
    }

}