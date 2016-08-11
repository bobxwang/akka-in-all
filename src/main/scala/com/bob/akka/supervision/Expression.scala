package com.bob.akka.supervision

/**
 * represents an arithmetic expression involving interger numbers
 */
trait Expression {
  def left: Expression

  def right: Expression
}

case class Add(left: Expression, right: Expression) extends Expression {
  override def toString: String = s"($left + $right)"
}

case class Multiply(left: Expression, right: Expression) extends Expression {
  override val toString = s"($left * $right)"
}

case class Divide(left: Expression, right: Expression) extends Expression {
  override val toString = s"($left / $right)"
}

case class Const(value: Int) extends Expression {
  def left = this

  def right = this

  override val toString = String.valueOf(value)
}