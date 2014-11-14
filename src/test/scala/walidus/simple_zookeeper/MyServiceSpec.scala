package walidus.simple_zookeeper

import org.specs2.mutable.Specification
import akka.actor.Props
import spray.http.StatusCodes.MethodNotAllowed
import spray.testkit.Specs2RouteTest
import walidus.simple_zookeeper.HomeService
import walidus.simple_zookeeper.MyService
import walidus.simple_zookeeper.MyServiceActor
import walidus.simple_zookeeper.PingService
import walidus.simple_zookeeper.PongService
import walidus.simple_zookeeper.Zoo

class ServicesSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system
  val myActor = actorRefFactory.actorOf(Props[TestDispatcherActor], "dispatcher-service")
  val homeS = actorRefFactory.actorOf(Props[TestHomeActor], "home-service")
  val pingS = actorRefFactory.actorOf(Props[TestPingActor], "ping-service")
  val pongS = actorRefFactory.actorOf(Props[TestPongActor], "pong-service")
//to myservice added trait actor and it don't compile, interesting, it worked without async evaluation
"MyService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> homeRoute ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> homeRoute ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(homeRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
    "return pong for ping GET requests" in {
      Get("/ping") ~> pingRoute ~> check {
        responseAs[String] must contain("pong")
      }
    }

    "return !? for pong GET requests" in {
      Get("/pong") ~> pongRoute ~> check {
        responseAs[String] must contain("!?")
      }
    }
  }
  
}
class TestPingActor extends PingService {
  val conf = Zoo.defaultConfiguration
}
class TestPongActor extends PongService {
  val conf = Zoo.defaultConfiguration
}
class TestHomeActor extends HomeService {
  val conf = Zoo.defaultConfiguration
}
class TestDispatcherActor extends MyServiceActor {
  val homeS = context.actorOf(Props[TestHomeActor], "home-service")
  val pingS = context.actorOf(Props[TestPingActor], "ping-service")
  val pongS = context.actorOf(Props[TestPongActor], "pong-service")
}
