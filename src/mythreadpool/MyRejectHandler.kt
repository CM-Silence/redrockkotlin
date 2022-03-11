package mythreadpool

class MyRejectHandler : RejectHandler {
    override fun reject(task : Runnable) {
        println("拒绝了$task")
    }
}