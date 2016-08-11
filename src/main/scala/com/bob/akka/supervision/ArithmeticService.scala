package com.bob.akka.supervision

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import com.bob.akka.supervision.FlakyExpressionCalculator.{FlakinessException, Result}

class ArithmeticService extends Actor with ActorLogging {

  override def receive: Receive = {
    case e: Expression =>
      val worker = context.actorOf(FlakyExpressionCalculator.props(expr = e, position = FlakyExpressionCalculator.Left))
      pendingWorkers += worker -> sender()
    case Result(originalExpression, value, _) =>
      notifyConsumerSuccess(worker = sender(), result = value)
  }

  var pendingWorkers = Map[ActorRef, ActorRef]()

  def notifyConsumerFailure(worker: ActorRef, failure: Throwable): Unit = {
    pendingWorkers.get(worker).foreach { x => x ! Status.Failure(failure) }
    pendingWorkers -= worker
  }

  def notifyConsumerSuccess(worker: ActorRef, result: Int): Unit = {
    pendingWorkers.get(worker) foreach {
      _ ! result
    }
    pendingWorkers -= worker
  }

  override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
    case _: FlakinessException =>
      log.warning("Evaluation of a top level expression failed, restarting.")
      Restart
    case e: ArithmeticException =>
      log.error("Evaluation failed because of: {}", e.getMessage)
      notifyConsumerFailure(worker = sender(), failure = e)
      Stop
    case e =>
      log.error("Unexpected failure: {}", e.getMessage)
      notifyConsumerFailure(worker = sender(), failure = e)
      Stop
  }

}