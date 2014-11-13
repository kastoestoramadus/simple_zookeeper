package walidus.simple_zookeeper

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout

class MyServiceActor extends MyService with KeptByZoo {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(homeRoute ~ pingRoute ~ pongRoute)
}
trait MyService extends Actor with HttpService {
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)
  val serviceName = "dispatcher"
  val homeS = context.actorOf(Props[HomeActor], "home-service")
  val pingS = context.actorOf(Props[PingActor], "ping-service")
  val pongS = context.actorOf(Props[PongActor], "pong-service")

  val homeRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          ctx: RequestContext => homeS ! ctx;
        }
      }
    }
  def pingRoute = path("ping") {
    get { ctx: RequestContext => pingS ! ctx; }
  }
  def pongRoute = path("pong") {
    get { ctx: RequestContext => pongS ! ctx }
  }
}