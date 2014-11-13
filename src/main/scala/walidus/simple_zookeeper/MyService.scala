package walidus.simple_zookeeper

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply

class MyServiceActor extends Actor with MyService with KeptByZoo {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(homeRoute ~ pingRoute ~ pongRoute)
}

trait MyService extends HttpService {
  val serviceName = "home"
  val homeRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                <h1>Say hello to services <i>/ping</i> and <i>/pong</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }
  def pingRoute = path("ping") {
    get { complete("pong!") }
  }
  def pongRoute = path("pong") {
    get { complete("pong!?") }
  }
}
