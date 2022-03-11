package mythreadpool

interface RejectHandler {
    fun reject(task : Runnable)
}