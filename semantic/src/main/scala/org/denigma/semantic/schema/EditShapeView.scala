package org.denigma.semantic.schema

import org.denigma.binding.binders.extractors.EventBinding
import org.denigma.binding.extensions._
import org.denigma.semantic.binders.RDFBinder
import org.denigma.semantic.models.WithShapeView
import org.denigma.semantic.shapes.{ArcView, ShapeView}
import org.denigma.semantic.store.FrontEndStore
import org.denigma.semweb.rdf.vocabulary.{RDF, WI}
import org.denigma.semweb.shex._
import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.raw.HTMLElement
import rx._
import rx.core.Var
import rx.ops._
import scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.util.{Failure, Success}

object EditShapeView
{

  //lazy val emptyArcRule: ArcRule =  ArcRule.apply(RDF.VALUE,WI.PLATFORM.EMPTY)

  def apply(elem:HTMLElement,mp:Map[String,Any]) = {
    new ShapeProperty(elem,mp)
  }


}

/**
 * View for Editing of shapes
 * @param elem
 * @param params
 */
class EditShapeView (val elem:HTMLElement,val params:Map[String,Any]) extends  ShapeView with WithShapeView
{

  lazy val title = this.shapeRes.map(_.stringValue)

  val saveClick = Var(EventBinding.createMouseEvent())

  protected def onSave() = {
    val sh = shapeInside.now
    shapeInside() = sh.copy(initial = sh.current)
  }

  saveClick.handler{
    onSave()
  }

  override def newItem(item:Item):ItemView = this.constructItemView(item,Map("item"->item)) { (e,m)=>
    ArcView.apply(e,m)
  }

  val onShapeChange = Obs(shapeInside,skipInitial = false){
    val cur = shapeInside.now.current
    updateShape(cur)
  }


  val addClick: Var[MouseEvent] = Var(EventBinding.createMouseEvent())

  val onAddClick = rx.extensions.AnyRx(addClick).handler{
    val item: Var[ArcRule] = Var(ArcRule.empty)
    this.rules() = rules.now + item
  }



  protected def onSaveFileClick(): Unit= {
    val sid = this.shapeRes.now
    val quads = this.shape.now.asQuads(this.shapeRes.now)
    FrontEndStore.write(quads,RDFBinder.defaultPrefixes.map(kv=>(kv._1,kv._2.stringValue)).toSeq:_*).onComplete{ //TODO: rewrite prefixes in views
      case Success(str)=>
        saveAs(sid.stringValue.substring(sid.stringValue.indexOf(":")+2)+".ttl",str)
      case Failure(th)=> dom.console.error("TURTLE DOWNLOAD ERROR: " +th)
    }
  }


  val saveFileClick= Var(EventBinding.createMouseEvent())

  saveFileClick.handler{
    onSaveFileClick()
  }



  override protected def attachBinders(): Unit = this.withBinders(ShapeView.defaultBinders(this))

  override def activateMacro(): Unit = { extractors.foreach(_.extractEverything(this))}



}

