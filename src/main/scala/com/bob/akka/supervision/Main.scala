package com.bob.akka.supervision

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}

object Main {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("calculator-system")
    val calculatorService = system.actorOf(Props[ArithmeticService], "arithmetic-service")

    def calculate(expr: Expression): Future[Int] = {
      implicit val timeoute = Timeout(1.second)
      (calculatorService ? expr).mapTo[Int]
    }

    // (3 + 5) / (2 * (1 + 1))
    val task = Divide(
      Add(Const(3), Const(5)),
      Multiply(
        Const(2),
        Add(Const(1), Const(1))
      )
    )

    val result = Await.result(calculate(task), 5.second)
    println(s"Got result: $result")

    Await.ready(system.terminate(), Duration.Inf)
  }
}