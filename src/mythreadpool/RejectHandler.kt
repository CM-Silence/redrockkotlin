package mythreadpool

interface RejectHandler {
    fun setReason(reason : String)
    fun reject(task : Runnable)
}