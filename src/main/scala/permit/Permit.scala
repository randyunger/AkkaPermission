package permit

import akka.actor.{ActorRef, Actor}
import collection.mutable


/**
 * Created with IntelliJ IDEA.
 * User: randy
 * Date: 12/13/12
 * Time: 3:21 PM
 */
class Permit extends Actor {

  val openTransactions = mutable.HashMap[Any, ActorRef]

  def receive = {
    case PermitRequest(r, a) => {
      println("received permit request")
      openTransactions += (a)
      r ? Permission(a)
    }

//    case PermittedResponse(r, a) => {
//      println("received permitted response")
//      //...
//    }
  }

}

case class Permission(a:Any)
case class PermitRequest(r: ActorRef, a:Any)
//case class PermittedResponse(r: ActorRef, a:Any)
