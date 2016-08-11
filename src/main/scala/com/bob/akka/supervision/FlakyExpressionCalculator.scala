package com.bob.akka.supervision

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor._
import com.bob.akka.supervision.FlakyExpressionCalculator.{FlakinessException, Position, Result}

import scala.concurrent.forkjoin.ThreadLocalRandom

/**
  *
  */
class FlakyExpressionCalculator(val expr: Expression, val position: Position) extends Actor with ActorLogging {

  override def receive: Receive = {
    case Result(_, value, position) if (expected(position)) =>
      expected -= position
      results += position -> value
      if (results.size == 2) {
        flakiness()
        val result: Int = evaluate(expr, results(FlakyExpressionCalculator.Left), results(FlakyExpressionCalculator.Right))
        log.info("Evaluated expression {} to value {}", expr, result)
        context.parent ! Result(expr, result, position)
        context.stop(self)
      }
    case Result(_, _, position) => throw new IllegalStateException(s"Expected results for positions ${expected.mkString(", ")} "
      + s"but got position $position")
  }

  override def supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
    case _: FlakinessException =>
      log.warning("Evaluation of {} failed, restarting.", expr)
      Restart
    case _ =>
      Escalate
  }

  var results = Map.empty[Position, Int]
  var expected = Set[Position](FlakyExpressionCalculator.Left, FlakyExpressionCalculator.Right)

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = expr match {
    case Const(value) =>
      context.parent ! Result(expr, value, position)
      context.stop(self)
    case _ =>
      context.actorOf(FlakyExpressionCalculator.props(expr.left, FlakyExpressionCalculator.Left), name = "left")
      context.actorOf(FlakyExpressionCalculator.props(expr.right, FlakyExpressionCalculator.Right), name = "right")
  }

  private def evaluate(expr: Expression, left: Int, right: Int): Int = expr match {
    case _: Add => left + right
    case _: Multiply => left * right
    case _: Divide => left / right
  }

  private def flakiness(): Unit =
    if (ThreadLocalRandom.current().nextDouble() < 0.2)
      throw new FlakinessException
}

object FlakyExpressionCalculator {

  def props(expr: Expression, position: Position): Props = Props(classOf[FlakyExpressionCalculator], expr, position)

  trait Position

  case object Left extends Position

  case object Right extends Position

  case class Result(originalExpression: Expression, value: Int, position: Position)

  class FlakinessException extends Exception("Flakiness")

}