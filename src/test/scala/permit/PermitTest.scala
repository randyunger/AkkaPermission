package permit

import org.specs2.mutable
import akka.actor.{Props, ActorSystem, Actor}
import collection.mutable.ListBuffer
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await.Awaitable

//Need to keep this for ? operator
import akka.dispatch.{DefaultPromise, Future, Await}

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
//    "receive a message" in {
//      op.processOrder(9834759, "fake.user")
//      1 must be equalTo(1)
//    }

//    "log with OrderDB" in {
//      op.processOrder(476456345, "fake.user2")
//      Thread.sleep(4000)
//      OrderDB.ordersByUsername("fake.user2") must have size(1)
//    }

//    "only execute once" in {
//      op.processOrder(476456345, "fake.user2")
//      op.processOrder(476456345, "fake.user2")
//      Thread.sleep(4000)
//      OrderDB.ordersByUsername("fake.user2") must have size(1)
//      println("DB ---- "+ OrderDB.db)
//    }

    "return the same id to client" in {
      val id1 = op.processOrder(476456345, "fake.user2")
      val id2 = op.processOrder(476456345, "fake.user2")
      Thread.sleep(4000)
      id1 must be equalTo id2
      println("id1: " + id1 + " id2: " + id2)
      println("DB ---- "+ OrderDB.db)
    }
  }
}

object OrderDB {

  val db: ListBuffer[OrderData] = ListBuffer.empty

  def logOrder(od: OrderData) { db.prepend(od) }
  def ordersByUsername(username: String) = db filter (_.username == username)
}


class OrderProc {

  implicit lazy val dur = 5000000 milli
  implicit lazy val timeout = Timeout(dur)

  def processOrder(upc:Long, username: String) = {
    println("requesting permission")
//    doProcessOrder(upc, username)
//    val x = Await.result(PermitTest.permitter ? PermitRequest(OrderData(upc, username)), dur)
//    val x = Await.result(PermitTest.orderProc ! OrderData(upc, username), dur)
    //PermitTest.orderProc ? OrderData(upc, username)
    val fut = PermitTest.orderProc ? OrderData(upc, username)
    println("fut: " + fut)
    fut.onComplete({
      case t:Throwable => println("err: " + t)
      case r:Any => println("res:: " + r)
    })

    val transId = Await.result(fut, dur).asInstanceOf[Awaitable[Any]]
    val x = Await.result(transId, dur).asInstanceOf[Awaitable[Any]]
    val y= Await.result(x, dur).asInstanceOf[Int]
//    val z= Await.result(y, dur).asInstanceOf[Awaitable[Any]]
//    println("Transaction ID: " + transId + " x: " + x + " y: " + y)
//    val z = Await.result(fut, dur).asInstanceOf[Int]
    println(transId)
//    println(z)
    y
//    transId
  }

  protected def doProcessOrder(upc:Long, username: String): Int = {
    println("processing order for " + username)
    println("Contacting bank")
    Thread.sleep(200)
    println("Charging account")
    Thread.sleep(200)
    println("Writing transaction to DB")
//    if (util.Random.nextBoolean) throw new Exception("random errrror")
    OrderDB.logOrder(OrderData(upc, username))
    Thread.sleep(1000)
    val transactionId = util.Random.nextInt(100)
    println("Created transaction " + transactionId +" for " + username)
    transactionId
  }
}

case class OrderData(upc: Long, username: String)
class OrderProcActor extends  OrderProc with Actor {
//  val orderProc = new OrderProc

  protected def receive = {
    case Permission(OrderData(upc, username)) => sender ! doProcessOrder(upc, username)//r ? PermittedResponse(self, doProcessOrder(upc, username))
    case OrderData(upc, username) => {
      val req = (PermitTest.permitter ? PermitRequest(self, OrderData(upc, username)))
//      val res = Await.result(Await.result(req, dur).asInstanceOf[Awaitable[Any]],dur)
//      sender ! res
      sender ! req
    }
    case a => println("received unknown: " + a)
  }
}
