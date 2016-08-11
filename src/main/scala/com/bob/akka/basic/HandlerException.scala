package com.bob.akka.basic

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait Message

case object MNormal extends Message

case object MError extends Message

/**
  * Created by bob on 16/8/11.
  */
class HandlerException extends Actor with ActorLogging {
  override def receive: Receive = {
    case MNormal => {
      val d = new Date()
      println(s"$d")
    }
    case MError => {
      sender ! "Fred"
    }
  }
}

object Handler {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("handler-system")
    val listener = system.actorOf(Props[HandlerException], name = "handler-exception")
    implicit val timeout = Timeout(5.seconds)
    (1 to 10).foreach(x => {
      if (x > 6 && x % 2 == 0) {
        val f = listener ? MError
        f.foreach(x => println(s"${x}--with"))
      } else {
        listener ! MNormal
      }
    })

    System.exit(0)
  }
}