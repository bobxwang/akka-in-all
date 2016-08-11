package com.bob.akka.basic

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait Message

case object MNormal extends Message

case object MError extends Message

class MHandlerException extends Exception("just for test")

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
      try {
        throw new MHandlerException
        sender ! "Some good result"
      } catch {
        case e: Exception => {
          sender ! akka.actor.Status.Failure(e)
          //          throw e
        }
      }
    }
  }
}

object Handler {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("handler-system")
    val listener = system.actorOf(Props[HandlerException], name = "handler-exception")
    implicit val timeout = Timeout(5.seconds)
    (1 to 10).foreach(x => {
      if (x == 4 || x == 6 || x == 9) {
        val f = listener ? MError
        f.onComplete(x => {
          if (x.isFailure) {
          }
        })
      } else {
        listener ! MNormal
      }
    })
    Thread.sleep(1000 * 10)
    System.exit(0)
  }
}