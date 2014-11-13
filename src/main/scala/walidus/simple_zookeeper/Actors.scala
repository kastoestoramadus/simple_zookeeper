package walidus.simple_zookeeper

import akka.actor.Actor
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.HttpMethods
import spray.http.Uri
import akka.actor.ActorRef
import spray.routing.RequestContext

class PingActor extends {val serviceName="ping"} with Actor with KeptByZoo{
  
  def receive = {
    case ctx: RequestContext =>
      ctx.complete("pong! with conf: "+conf)
  }
}

class PongActor extends {val serviceName="pong"} with Actor with KeptByZoo{
  def receive = {
    case ctx: RequestContext =>
      ctx.complete("pong!? with conf: "+conf)
  }
}

class HomeActor extends {val serviceName="home"} with Actor with KeptByZoo{
  def receive = {
    case ctx: RequestContext =>
      ctx.complete( """<html>
              <body>
                Say hello to services <i>/ping</i> and <i>/pong</i>!
              </body>
            </html>""")
  }
}