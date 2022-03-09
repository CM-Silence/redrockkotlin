package test2

import java.util.Scanner
import java.math.BigDecimal
import java.math.RoundingMode

//work0 level2
//彩票中奖概率计算器
fun main() {
    val sc = Scanner(System.`in`)
    var num1 = 1.0
    var num2 = 1.0
    println("从n个数字中抽取k个数字,中奖概率的计算awa")
    do {
        println("请输入n的值:")
        val n = judge()
        println("请输入k的值:")
        val k = judge(n)

        //繁琐的数学计算awa
        for (i in 1 .. k){
            num2 *= i
        }
        for (i in n downTo n - k + 1){
            num1 *= i
        }
        val num3 = BigDecimal(num1)
        val num4 = BigDecimal(num2)
        println("没用BigDecimal类中方法计算出的中奖概率为:${num2 / num1}")
        println("使用BigDecimal类中方法计算出的中奖概率为:${num4.divide(num3,20,RoundingMode.HALF_UP)}")
        println("要再玩一次吗?(输入yes再次游玩)")
    }while(sc.next() == "yes")
}

//用于判断用户输入是否为正整数
fun judge(n : Int = 0) : Int{
    val sc = Scanner(System.`in`)
    var num : Int
    do {
        if (sc.hasNextInt()){ //首先判断是否是整数
            num = sc.nextInt()
            if ((n == 0 && num >= 0) || num in 0 .. n) {
                return num
            }
            else{
                println("请输入一个在0-n范围内的整数!")
            }
        }
        else{
            sc.next() //清除用户输出
            println("请输入一个正整数!")
        }
    }while (true)
}