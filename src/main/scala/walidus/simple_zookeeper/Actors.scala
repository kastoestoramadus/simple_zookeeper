package walidus.simple_zookeeper

import akka.actor.Actor
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.HttpMethods
import spray.http.Uri
import akka.actor.ActorRef
import spray.routing.RequestContext

class PingActor extends PingService with KeptByZoo {conf}

trait PingService extends Actor{
  val conf:String
  val serviceName="ping"
  def receive = {
    case ctx: RequestContext =>
      ctx.complete("pong! with conf: "+conf)
  }
}
class PongActor extends PongService with KeptByZoo {conf}

trait PongService extends Actor{
  val conf:String
  val serviceName="pong"
  def receive = {
    case ctx: RequestContext =>
      ctx.complete("pong!? with conf: "+conf)
  }
}

class HomeActor extends HomeService with KeptByZoo {conf}

trait HomeService extends Actor{
  val serviceName="home"
    def receive = {
    case ctx: RequestContext =>
      ctx.complete( """<html>
              <body>
                Say hello to services <i>/ping</i> and <i>/pong</i>!
              </body>
            </html>""")
  }  
}