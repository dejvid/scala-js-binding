package org.denigma.graphs

import org.denigma.binding.extensions._
import org.denigma.binding.messages.GraphMessages
import org.denigma.binding.picklers.rp
import org.denigma.binding.views.BindableView
import org.denigma.semantic.storages.Storage
import org.scalajs.dom
import org.scalajs.dom.HTMLElement
import org.scalajs.sigma.{SigmaEdge, SigmaNode}
import org.scalajs.spickling.PicklerRegistry
import org.scalajs.threejs._
import org.scalax.semweb.rdf.{IRI, Quad, Res}
import org.scalax.semweb.sparql.Pat

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic


abstract class GraphView(val elem:HTMLElement,val params:Map[String,Any]) extends BindableView
{
  lazy val containerId = this.resolveKeyOption("graph-container"){
    case cont:String=>cont
  }.getOrElse("graph-container")

  lazy val container: HTMLElement  = dom.document.getElementById(containerId)

  lazy val graph =     new GraphContainer(container,1000,1000)


  override def bindView(el:HTMLElement) =
  {
    super.bindView(el)
    graph.render()
  }


}