package walidus.simple_zookeeper

import scala.concurrent.duration.DurationInt

import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger

import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
object Boot extends App {
  BasicConfigurator.configure()
  Logger.getRootLogger().setLevel(Level.WARN);

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  Zoo.initZoo({
    val retryPolicy: RetryPolicy = new RetryOneTime(300)
    CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
      .retryPolicy(retryPolicy).build()
  }) // such form works easy with tests
  
  // create and start our service actor
  val service = system.actorOf(Props[DispatcherActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
