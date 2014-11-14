package walidus.simple_zookeeper

import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.Timeout
import spray.http.MediaTypes._
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.RequestContext

class DispatcherActor extends MyServiceActor with KeptByZoo {
  val homeS = context.actorOf(Props[HomeActor], "home-service")
  val pingS = context.actorOf(Props[PingActor], "ping-service")
  val pongS = context.actorOf(Props[PongActor], "pong-service")
  registerInZoo()
}
// trait created to enable easy testing
trait MyServiceActor extends Actor with MyService {
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)
  val myActor: ActorRef = self

  def actorRefFactory = context

  def receive = runRoute(homeRoute ~ pingRoute ~ pongRoute)
}

trait MyService extends HttpService {
  val serviceName = "dispatcher"
  val myActor: ActorRef
  val homeS: ActorRef
  val pingS: ActorRef
  val pongS: ActorRef

  val homeRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          ctx: RequestContext => homeS.tell(ctx, myActor)
        }
      }
    }
  def pingRoute = path("ping") {
    get { ctx: RequestContext => pingS.tell(ctx, myActor) }
  }
  def pongRoute = path("pong") {
    get { ctx: RequestContext => pongS.tell(ctx, myActor) }
  }
}