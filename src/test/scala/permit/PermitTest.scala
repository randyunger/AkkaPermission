package permit

import org.specs2.mutable
import akka.actor.{Props, ActorSystem, Actor}
import collection.mutable.ListBuffer
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

//Need to keep this for ? operator
import akka.dispatch.Await

/**
 * Created with IntelliJ IDEA.
 * User: randy
 * Date: 12/13/12
 * Time: 3:21 PM
 */

object PermitTest {
  val system = ActorSystem("System")
  val permitter = PermitTest.system.actorOf(Props[Permit])
  val orderProc = PermitTest.system.actorOf(Props[OrderProcActor])
}

class PermitTest extends mutable.Specification {
  val op = new OrderProc                                      //Don't like having two!

  "Permit" should {
    "receive a message" in {
      op.processOrder(9834759, "fake.user")
      1 must be equalTo(1)
    }

//    "log with OrderDB" in {
//      op.processOrder(476456345, "fake.user2")
//      Thread.sleep(4000)
//      OrderDB.ordersByUsername("fake.user2") must have size(1)
//    }

    "only execute once" in {
      op.processOrder(476456345, "fake.user2")
      op.processOrder(476456345, "fake.user2")
      Thread.sleep(4000)
      OrderDB.ordersByUsername("fake.user2") must have size(1)
      println(OrderDB.db)
    }
  }
}

object OrderDB {

  val db: ListBuffer[OrderData] = ListBuffer.empty

  def logOrder(od: OrderData) { db.prepend(od) }
  def ordersByUsername(username: String) = db filter (_.username == username)
}


class OrderProc {

  implicit lazy val dur = 100 milli
  implicit lazy val timeout = Timeout(dur)

  def processOrder(upc:Long, username: String) {
    println("requesting permission")
//    doProcessOrder(upc, username)
//    val x = Await.result(PermitTest.permitter ? PermitRequest(OrderData(upc, username)), dur)
//    val x = Await.result(PermitTest.orderProc ! OrderData(upc, username), dur)
    //PermitTest.orderProc ? OrderData(upc, username)
  val fut = PermitTest.orderProc ? OrderData(upc, username)
  println("fut: " + fut)
//    println("received x response " + x)
  }

  protected def doProcessOrder(upc:Long, username: String) = {
    println("processing order for " + username)
    println("Contacting bank")
    Thread.sleep(200)
    println("Charging account")
    Thread.sleep(200)
    println("Writing transaction to DB")
    OrderDB.logOrder(OrderData(upc, username))
    Thread.sleep(1000)
    val cost = util.Random.nextInt(100)
    println("Charged " + cost +" to " + username)
    cost
  }
}

case class OrderData(upc: Long, username: String)
class OrderProcActor extends  OrderProc with Actor {
//  val orderProc = new OrderProc

  protected def receive = {
    case Permission(OrderData(upc, username)) => sender ! doProcessOrder(upc, username)//r ? PermittedResponse(self, doProcessOrder(upc, username))
    case OrderData(upc, username) => PermitTest.permitter ? PermitRequest(self, OrderData(upc, username))
    case a => println("received unknown: " + a)
  }
}
