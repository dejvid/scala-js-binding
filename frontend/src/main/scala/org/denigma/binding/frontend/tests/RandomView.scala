package org.denigma.binding.frontend.tests

import org.denigma.binding.views.BindableView
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{ MouseEvent}
import rx.Rx
import rx.core.Var

import scala.collection.immutable.Map
import scala.util.Random
import scalatags.Text.tags._
import scalatags.Text.{attrs => a, TypedTag}
import scalatags.Text.implicits._

/**
 * For test purposes only
 */
class RandomView(val elem:HTMLElement, val params:Map[String,Any]) extends BindableView{


  val counting: Var[TypedTag[String]] = Var{

    div(a.`class`:= "ui segment",
      h1("This is title"),
      p("value that changes: 'START'")
    )
  }

  val foo= Var{"Foo variable text"}
  val bar = Var{"Bar variable text"}


  val list = List("ONE","TWO","THREE","FOUR","SOME TEXT","THAT IS RANDOM")

  def update():Unit ={
    val value =  div(a.`class`:="ui segment",
      h1("This is title"),
      p(s"value that changes: '${list(Random.nextInt(list.length))}' ")
    )
    counting() = value

  }

    override def activateMacro(): Unit = { extractors.foreach(_.extractEverything(this))}

  override protected def attachBinders(): Unit = binders = BindableView.defaultBinders(this)


  dom.setInterval(update _, 100)

  /** Computes the square of an integer.
    *  This demonstrates unit testing.
    */
  def square(x: Int): Int = x*x

}
