package permit

import akka.actor.{ActorRef, Actor}
import collection.mutable
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Future


/**
 * Created with IntelliJ IDEA.
 * User: randy
 * Date: 12/13/12
 * Time: 3:21 PM
 */
class Permit extends Actor {

  implicit lazy val dur = 10000 milli
  implicit lazy val timeout = Timeout(dur)

  val openTransactions: mutable.HashMap[Any, Future[Any]] = mutable.HashMap.empty

  def receive = {
    case PermitRequest(r, a) => {
      println("received permit request. Reqs: " + openTransactions)
      val future = if (openTransactions.contains(a)) {
        println("An open transaction already exists for " + a)
        openTransactions(a)
      } else {
        println("Creating a new transaction for " + a)
        val f = r ? Permission(a)
        openTransactions += (a -> f)
        f
      }
      sender ! future
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
