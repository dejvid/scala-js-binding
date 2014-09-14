package org.denigma.semantic.models

import org.denigma.binding.binders.{GeneralBinder, NavigationBinding}
import org.denigma.binding.views.BindableView
import org.denigma.semantic.binders.ModelBinder
import org.denigma.semantic.storages.AjaxModelStorage
import org.scalajs.dom
import org.scalax.semweb.rdf.Res
import org.scalax.semweb.shex.PropertyModel

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

object PropertyModelView
{
  def defaultBinders(view:PropertyModelView)  =new ModelBinder(view,view.model)::new GeneralBinder(view)::new NavigationBinding(view)::Nil

}

trait PropertyModelView extends ModelView with BindableView

trait  AjaxModelView extends PropertyModelView
{
  def resource: Res
  def shapeRes: Res



  def storage:AjaxModelStorage



  def saveModel() = {
    if(this.model.now.isUnchanged)
    {
      dom.console.log("trying to save unchanged model")
    }
    else {
      storage.update(this.shapeRes,overWrite = true)(model.now.current).onComplete{
        case Failure(th)=>
          dom.console.error(s"failure in saving of movel with channel $storage.channel: \n ${th.getMessage} ")
        case Success(bool)=>
        {
          if(bool) this.model() = this.model.now.refresh else dom.console.log(s"the model was not saved")
        }

      }
    }
  }


  /**
   * Handler on model load
   * @param items
   */
  protected def onLoadModel(items:List[PropertyModel]) = {
    if(items.size>1) dom.console.error(s"more than one model received from ${storage.channel} for onemodel binding")
    val m = items.head
    this.model() = this.model.now.copy(m,m)
  }

}
