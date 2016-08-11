package com.bob.akka.basic

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool

import scala.concurrent.duration._

sealed trait PiMessage

case object Calculate extends PiMessage

case class Work(start: Int, nrOfElements: Int) extends PiMessage

case class Result(value: Double) extends PiMessage

case class PiApproximation(pi: Double, duration: Duration)

class Worker extends Actor {

  def calculatePiFor(start: Int, nrOfElements: Int): Double = {
    var acc = 0.0
    for (i <- start until (start + nrOfElements))
      acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
    acc
  }

  override def receive: Receive = {
    case Work(start, nrOfElements) => sender ! Result(calculatePiFor(start, nrOfElements))
  }
}

class Listener extends Actor {
  override def receive: Receive = {
    case PiApproximation(pi, duration) => {
      println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s"
        .format(pi, duration))
      context.system.terminate()
    }
  }
}

/**
  *
  * @param nrOfWorkers  定义启动多少个工作actor
  * @param nrOfMessages 定义多少整数段发送给工作actor
  * @param nrOfElements 定义发送给工作actor的每个整数段大小
  * @param listener     用来向外界报告最终计算结果
  */
class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef) extends Actor {

  var pi: Double = _
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis

  val workerRouter = context.actorOf(
    Props[Worker].withRouter(RoundRobinPool(nrOfWorkers)), name = "workerRouter")

  override def receive: Receive = {
    case Calculate => {
      for (i <- 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
    }
    case Result(value) => {
      pi += value
      nrOfResults += 1
      if (nrOfResults == nrOfMessages) {
        listener ! PiApproximation(pi, duration = (System.currentTimeMillis - start).millis)
        context.stop(self)
      }
    }
  }
}

/**
  * 计算派尔 -- 3.1415926
  */
object Pi extends App {

  calculate(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 10000)

  def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    // Create an Akka system
    val system = ActorSystem("PiSystem")

    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")
    // listener path will be: akka://PiSystem/user/listener

    // create the master
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfMessages, nrOfElements, listener)),
      name = "master")

    // start the calculation
    master ! Calculate
  }
}

/**
  * 一个Actor是一个容器，包含了状态，行为，邮箱，子Actor及一个监管策略
  * 监管者将任务委托给下属并对下属的失败状况进行响应
  **/
