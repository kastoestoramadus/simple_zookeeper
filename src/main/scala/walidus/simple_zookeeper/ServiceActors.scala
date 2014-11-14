package walidus.simple_zookeeper

import akka.actor.Actor
import spray.routing.RequestContext

class PingActor extends PingService with KeptByZoo { registerInZoo() }

trait PingService extends Actor {
  val conf: Configuration
  val serviceName = "ping"
  def receive = {
    case ctx: RequestContext =>
      ctx.complete("pong! with conf: " + conf)
  }
}
class PongActor extends PongService with KeptByZoo { registerInZoo() }

trait PongService extends Actor {
  val conf: Configuration
  val serviceName = "pong"
  def receive = {
    case ctx: RequestContext =>
      ctx.complete("pong!? with conf: " + conf)
  }
}

class HomeActor extends HomeService with KeptByZoo { registerInZoo() }

trait HomeService extends Actor {
  val conf: Configuration
  val serviceName = "home"
  def receive = {
    case ctx: RequestContext =>
      ctx.complete("""<html>
              <body>
                Say hello to services <i>/ping</i> and <i>/pong</i>!
              </body>
            </html>""")
  }
}