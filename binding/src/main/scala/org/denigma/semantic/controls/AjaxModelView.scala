package org.denigma.semantic.controls

import org.denigma.semantic.binding.ActiveModelView
import org.denigma.semantic.storages.AjaxModelStorage
import org.scalajs.dom
import org.scalax.semweb.rdf.Res
import org.scalax.semweb.shex.PropertyModel

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

trait  AjaxModelView extends ActiveModelView
{
  def resource: Res
  def shapeRes: Res



  def storage:AjaxModelStorage




  def saveModel() = {
    if(this.modelInside.now.isUnchanged)
    {
      dom.console.log("trying to save unchanged model")
    }
    else {
      storage.update(this.shapeRes,overWrite = true)(modelInside.now.current).onComplete{
        case Failure(th)=>
          dom.console.error(s"failure in saving of movel with channel $storage.channel: \n ${th.getMessage} ")
        case Success(bool)=>
        {
          if(bool) this.modelInside() = this.modelInside.now.refresh else dom.console.log(s"the model was not saved")
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
    val model = items.head
    this.modelInside() = this.modelInside.now.copy(model,model)
  }

}
