package walidus.simple_zookeeper

import spray.json.DefaultJsonProtocol

case class Configuration(comment: String, port: Int, timeout: Long)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val configurationFormat = jsonFormat3(Configuration)
}