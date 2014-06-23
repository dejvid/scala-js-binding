package org.denigma.binding.views

import org.denigma.binding.extensions._
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.extensions._
import rx.Rx
import rx.extensions.Moved

import scala.collection.immutable._
import scala.scalajs.js


trait CollectionView extends OrdinaryView{

  type Item
  type ItemView <: BindingView

  val items:Rx[List[Item]]

  var template: HTMLElement = viewElement

  lazy val span = this.extractStart() //all items are inserted after it
  /**
   * extracts element after wich it inserts children
   * @return
   */
  def extractStart(): HTMLElement = {
    val id = "items_of_"+this.viewElement.id
    sq.byId(id) match {
      case Some(el)=>el
      case None=>
        val sp = document.createElement("span")
        sp.id = id
        if(template==viewElement) {
          viewElement.appendChild(sp)
          alert(id)
        }
        else this.replace(sp,template)
        sp
    }

  }

  /**
   * Replaces elementes
   * @param newChild new child
   * @param oldChild oldchild
   * @return
   */
  def replace(newChild:HTMLElement,oldChild:HTMLElement, switch:Boolean = false) = if(oldChild!=newChild)   (newChild.parentElement, oldChild.parentElement) match {
     case (pn,null)=>
       console.error("old child has not parent")
       oldChild
     case (null,po)=>
       po.replaceChild(newChild,oldChild)
       if(switch) console.error("new child has null parent")

     case (pn,po) if pn==po=>
       if(switch) {
         val io = po.children.indexOf(oldChild)
         val in = pn.children.indexOf(newChild)
         //po.removeChild(oldChild)
         po.children(io) = newChild
         pn.children(in) = oldChild
         //console.error("new child has null parent")
       } else  po.replaceChild(newChild,oldChild)

     case (pn,po) =>
       val io = po.children.indexOf(oldChild)
       val in = pn.children.indexOf(newChild)
       //po.removeChild(oldChild)
       po.children(io) = newChild
       if(switch) {
         pn.children(in) = oldChild
       }


  }

  /**
   * Binds nodes to the element
   * @param el
   */
  override def bind(el:HTMLElement):Unit =   if(el.attributes.contains("data-template")) {
    el.removeAttribute("data-template")
    this.template = el
  } else this.viewFrom(el) match {

    case Some(view) if el.id.toString!=this.id =>
      this.subviews.getOrElse(el.id, this.createView(el,view))

    case _=>
      this.bindElement(el)
      if(el.hasChildNodes()) el.childNodes.foreach {
        case el: HTMLElement => this.bind(el)
        case _ => //skip
      }
  }

  lazy val updates = Watcher(items).updates


  protected def onInsert(item:Item) = this.addItemView(item,this.newItem(item))
  protected def onRemove(item:Item) = this.removeItemView(item)
  protected def onMove(mv:Moved[Item]) = {
    val fr = itemViews(items.now(mv.from))
    val t = itemViews(items.now(mv.to))
    this.replace(t.viewElement,fr.viewElement)
  }

  /**
   * Adds subscription
   */
  protected   def subscribeUpdates(){
    this.items.now.foreach(i=>this.addItemView(i,this.newItem(i)))
    updates.handler{
      updates.now.inserted.foreach(onInsert)
      updates.now.removed.foreach(onRemove)
      updates.now.moved.foreach(onMove)
      }
  }




  def newItem(mp:Item):ItemView

  var itemViews = Map.empty[Item,ItemView]

  def addItemView(item:Item,iv:ItemView):ItemView = {
    span.parentElement.insertBefore(iv.viewElement)
    this.addView(iv)
    iv.bindView(iv.viewElement)
    itemViews = itemViews + (item->iv)
    iv
  }

  def removeItemView(r:Item) =  this.itemViews.get(r) match {
    case Some(rv)=>
      rv.unbindView()
      this.removeView(rv.id)
      this.itemViews = itemViews - r
    case None=>
      dom.console.error("cantot find the view for item: "+r.toString+" in map "+this.itemViews.toString())

  }




}