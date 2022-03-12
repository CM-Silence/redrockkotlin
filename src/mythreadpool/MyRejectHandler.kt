package mythreadpool

class MyRejectHandler : RejectHandler {
    private var reason : String = ""

    //设置拒绝原因
    override fun setReason(reason : String){
        this.reason = reason
    }

    //拒绝策略
    override fun reject(task : Runnable) {
        println("拒绝了${task},原因:$reason") //这里简单地打印一下即可
    }
}