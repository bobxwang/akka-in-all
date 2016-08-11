package com.bob.akka.basic

import akka.actor.Actor

case class ForceRestart(value: String)

/**
  * Actor lifecycle
  */
class LifecycleActor extends Actor {

  println("entered the Kenny constructor")

  /**
    * 在actor实例化后执行，重启时不会执行
    */
  override def preStart(): Unit = {
    println("kenny: preStart")
  }

  /**
    * 在actor正常终止后执行，异常重启时不会执行
    */
  override def postStop(): Unit = {
    println("kenny: postStop")
  }

  /**
    * 在actor异常重启前保存当前状态
    *
    * @param reason
    * @param message
    * @throws java.lang.Exception
    */
  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("kenny: preRestart")
    println(s" MESSAGE: ${message.getOrElse("")}")
    println(s" REASON: ${reason.getMessage}")

    super.preRestart(reason, message)
  }

  /**
    * 在actor异常重启后恢复重启前保存的状态
    * 当异常引起了重启，新actor的postRestart方法被觩发，默认情况下preStart方法被调用
    *
    * @param reason
    * @throws java.lang.Exception
    */
  @throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    println("kenny: postRestart")
    println(s" REASON: ${reason.getMessage}")

    super.postRestart(reason)
  }

  override def receive: Actor.Receive = {
    case ForceRestart => throw new Exception("Boom!")
    case _ => println("kenny received a message")
  }
}