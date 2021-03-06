package controllers.endpoints

import controllers.PjaxController
import org.denigma.binding.messages.ExploreMessages
import org.denigma.binding.messages.ExploreMessages.ExploreMessage
import org.denigma.endpoints.{PrickleController, AjaxExploreEndpoint, AuthRequest, UserAction}
import org.denigma.semweb.shex.PropertyModel
import play.api.libs.json.Json
import play.api.mvc.Result
import prickle.{Pickle, Unpickle}

import scala.concurrent.Future

/**
 * Explore articles trait
 */
trait ExploreEndpoint extends PrickleController with AjaxExploreEndpoint with Items
{
  self:PjaxController=>
  import org.denigma.binding.composites.BindingComposites
  import org.denigma.binding.composites.BindingComposites._

  override type ExploreRequest = AuthRequest[ExploreMessage]

  override type ExploreResult = Future[Result]




  def exploreEndpoint() = UserAction.async(this.unpickleWith{
    str=>
      Unpickle[ExploreMessage](BindingComposites.exploreMessages.unpickler).fromString(str)
  }){implicit request=>
    this.onExploreMessage(request.body)

  }

  protected def exploreItems(items:List[PropertyModel],exploreMessage:ExploreMessages.Explore): List[PropertyModel] = {
    val list: List[PropertyModel] = items.filter{case a=>
      val res = exploreMessage.filters.forall(_.matches(a))
      res
    }

    exploreMessage.sortOrder match {
      case Nil=>list
      case s::xs=>
        //play.Logger.debug("sort takes place")

        if(list.nonEmpty) list.head::Nil
        else list.sortWith{case (a,b)=>s.sort(xs)(a,b) > -1}

    }

  }

  override def onExplore(exploreMessage: ExploreMessages.Explore)(implicit request: ExploreRequest): ExploreResult = {

    this.items.get( exploreMessage.shape)  match {
      case Some(list)=> this.shapes.get(exploreMessage.shape) match {
        case Some(shape)=>

          //val res = rp.pickle( ExploreMessages.Exploration(shape,list,exploreMessage) )
          //Future.successful(Ok(res).as("application/json"))
          val res = Pickle.intoString(ExploreMessages.Exploration(shape,list,exploreMessage) )
          (BindingComposites.explorationPickler,BindingComposites.config)
          Future.successful(this.pack(res))

        case None=> this.onBadExploreMessage(exploreMessage,s"cannot find items for ${exploreMessage.channel}")
      }
      case None=>this.onBadExploreMessage(exploreMessage,s"cannot find items for ${exploreMessage.channel}")
    }
  }


  protected def suggest(suggestMessage: ExploreMessages.ExploreSuggest, items:List[PropertyModel])(implicit request: ExploreRequest): ExploreResult = {
    //play.Logger.debug("original = "+suggestMessage.toString)
    val t = suggestMessage.typed
    val name = suggestMessage.nameClass
    val list: Seq[PropertyModel] = exploreItems(items,suggestMessage.explore)

    //play.Logger.debug("basic list = "+suggestMessage.toString)

/*    val result = list.collect { case item  =>
      item.properties.collect{case (key,values) if name.matches(key)=> values.filter(v=>v.contains(t))
        /*.collect {
        case p if p.stringValue.contains(t) => p
      }
    }.flatten
*/
      val mes = ExploreMessages.ExploreSuggestion(t, result, suggestMessage.id, suggestMessage.channel, new Date())
      val p = rp.pickle(mes)
      Future.successful(Ok(p).as("application/json"))*/
    ???

  }



  override def onExploreSuggest(suggestMessage: ExploreMessages.ExploreSuggest)(implicit request: ExploreRequest): ExploreResult = {

    this.items.get( suggestMessage.explore.shape)  match
    {
      case Some(list)=>this.suggest(suggestMessage, list)(request)
      case None=> this.onBadExploreMessage(suggestMessage)
    }
  }

  override def onBadExploreMessage(message: ExploreMessages.ExploreMessage, reason:String)(implicit request: ExploreRequest): ExploreResult ={

    Future.successful(BadRequest(Json.obj("status" ->"KO","message"->reason)).as("application/json"))

  }

  override def onSelect(suggestMessage: ExploreMessages.SelectQuery)(implicit request: ExploreRequest): ExploreResult = {
    this.items.get( suggestMessage.shapeId)  match {

      case Some(list)=>
        val l = Pickle.intoString(list)
        Future.successful(pack(l))

      case None=> this.onBadExploreMessage(suggestMessage,"cannot find shape for the message")
    }




  }

  override def onBadExploreMessage(message: ExploreMessage)(implicit request: ExploreRequest): ExploreResult = Future.successful(BadRequest(Json.obj("status" ->"KO","message"->"wrong message type!")).as("application/json"))


}
