package test1

import java.util.Scanner

//work0 level1
//来自卷王的邀请
fun main() {
    val sc = Scanner(System.`in`) //开局先用val,val用不了再考虑var
    do{
        println("请输入你的平时成绩:")
        val ug = judge()
        println("请输入你的期中成绩:")
        val meg = judge()
        println("请输入你的期末成绩:")
        val feg = judge()
        val fg = ug * 0.2 + meg * 0.3 + feg * 0.5
        if (fg < 60){
            println("勇敢俊枭,不怕困难")
        }
        else{
            println("就这?还没我卷")
        }
        println("是否要再玩一次?(输入yes重新开始)")

    }while (sc.next() == "yes")
}

//用于判断用户输入是否为正整数
fun judge() : Int{
    val sc = Scanner(System.`in`)
    var num : Int
    do {
        if (sc.hasNextInt()){ //首先判断是否是整数
            num = sc.nextInt()
            if (num in 0..100) {
                return num
            }
            else{
                println("请输入一个在0-100之间的整数!")
            }
        }
        else{
            sc.next() //清除用户输出
            println("请输入一个在0-100之间的整数!")
        }
    }while (true)
}