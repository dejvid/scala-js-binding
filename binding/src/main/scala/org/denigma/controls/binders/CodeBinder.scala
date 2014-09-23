package org.denigma.controls.binders

import org.denigma.binding.binders.GeneralBinder
import org.denigma.binding.views.BindableView
import org.scalajs.codemirror.{EditorConfiguration, CodeMirror, Editor}
import org.scalajs.dom
import org.scalajs.dom.HTMLElement
import org.scalajs.jquery._
import rx._
import org.scalajs.codemirror.{CodeMirror, EditorConfiguration, Editor}

import scala.collection.immutable.Map
import scala.scalajs.js
import org.denigma.binding.extensions._
import rx.ops._

class CodeBinder(view:BindableView) extends GeneralBinder(view:BindableView)
{

  var editors = Map.empty[HTMLElement,Editor]

  override def bindProperties(el:HTMLElement,ats:Map[String, String]): Unit = for {
    (key, value) <- ats
  }{
    this.visibilityPartial(el,value)
      .orElse(this.classPartial(el,value))
      .orElse(this.codePartial(el,value,ats))
      .orElse(this.propertyPartial(el,key.toString,value))
      .orElse(this.upPartial(el,key.toString,value))
      .orElse(this.otherPartial)(key.toString)//key.toString is the most important!
  }

  def codePartial(el:HTMLElement,value:String,ats:Map[String, String]):PartialFunction[String,Unit] = {
    case "bind-code"=> ats.get("mode") match {
      case Some(m)=> this.makeCode(el,value,m)
      case None=> this.makeCode(el,value,"htmlmixed")
    }

    case "bind-code-html"=>
      this.makeCode(el,value,"htmlmixed")

    case "bind-code-sparql"=> makeCode(el,value,"application/x-sparql-query")
  }

  def makeEditor(area:dom.HTMLTextAreaElement,textValue:String,codeMode:String) = {
    val params = js.Dynamic.literal(
      mode = codeMode,
      lineNumbers = true,
      value = textValue
    )
    CodeMirror.fromTextArea(area,params.asInstanceOf[EditorConfiguration])
  }

  def onChange(code:Var[String])(ed:Editor)
  {
    val v =  ed.getDoc().getValue()
    if(code.now!=v)  code() = v
  }

  def makeCode(el:HTMLElement,value:String,mode:String):Unit = this.strings.get(value) match {
    case Some(str:Var[String])=>
      if(str.now=="")
      {
        if(el.innerHTML!=""){
          val t = jQuery(el).text()
          str() = t
          el.innerHTML = ""
        }
      }

      this.makeCode(el,str,mode)
    case Some(str)=> this.makeCode(el,str,mode)
    case None=> error(s"cannot find code string $value in $id")
  }


  def makeCode(el:HTMLElement, str:Rx[String], mode:String):Unit = el match {
      case area: dom.HTMLTextAreaElement =>
        this.editors.get(area) match {
          case Some(ed) =>
            ed.getDoc().setValue(str.now)

          case None =>
            val ed = this.makeEditor(area, str.now, mode)
            this.editors = this.editors + (area -> ed)
            if(str.now!="") ed.getDoc().setValue(str.now)
            str match {
              case s: Var[String] => ed.on("change", onChange(s) _)
              case _ => this.info(s"$str.now is not reactive Var in $id")
            }
            str.handler{
              val d = ed.getDoc()
              if(d.getValue()!=str.now) d.setValue(str.now)
            }

        }

    case _=> error(s"cannot find code string ${str.now} in $id")
  }


}
