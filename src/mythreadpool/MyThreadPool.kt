package mythreadpool

import java.util.concurrent.*

class MyThreadPool(corePoolSize : Int, maximumPoolSize :Int, keepAliveTime : Long, unit : TimeUnit,
                   workQueue : BlockingQueue<Runnable>, threadFactory : ThreadFactory = Executors.defaultThreadFactory(), handler : RejectedExecutionHandler) {

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

}