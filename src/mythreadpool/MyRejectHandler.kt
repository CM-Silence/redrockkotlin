package mythreadpool

class MyRejectHandler : RejectHandler {
    //拒绝策略
    override fun reject(task : Runnable) {
        println("拒绝了$task") //这里简单地打印一下即可
    }
}