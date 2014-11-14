package walidus.simple_zookeeper

import java.net.InetAddress
import java.net.Socket
import java.util.regex.PatternSyntaxException
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.ZooDefs.Ids
import scala.concurrent.duration.Duration
import spray.json._
import DefaultJsonProtocol._

object Zoo {

  var client: CuratorFramework = null

  def initZoo(c: CuratorFramework): Unit = {
    client = c
    if (serverListening(connectionHost, connectionPort)) client.start()
    if (isZooOpen) {
      if (client.checkExists().forPath("/services") == null)
        client.create().withACL(Ids.OPEN_ACL_UNSAFE).forPath("/services")
      if (client.checkExists().forPath("/services/runtime") == null)
        client.create().withACL(Ids.OPEN_ACL_UNSAFE).forPath("/services/runtime")
      if (client.checkExists().forPath("/services/configuration") == null)
        client.create().withACL(Ids.OPEN_ACL_UNSAFE)
          .forPath("/services/configuration", "Client configuration with port 7".getBytes())
    }
  }

  def getConnectString = client.getZookeeperClient().getCurrentConnectionString()

  def createZooKeeperClient() = client.getZookeeperClient()
  def isZooOpen(): Boolean =
    client.getState() == CuratorFrameworkState.STARTED &&
      serverListening(connectionHost, connectionPort)
  private def connectionHost: String = getConnectString.split(':')(0)
  private def connectionPort: Int = Integer.parseInt(getConnectString.split(':')(1))
  private def serverListening(host: String, port: Int): Boolean = {
    var s: Socket = null;
    try {
      s = new Socket(host, port); true
    } catch {
      case e: Exception => false;
    } finally {
      if (s != null) try { s.close() } catch { case _: Throwable => }
    }
  }
}
trait KeptByZoo {
  val serviceName: String
  val clientDesc = serviceName + '_' + InetAddress.getLocalHost().getCanonicalHostName()
  lazy val conf: String = registerAndGetConf

  private def registerAndGetConf(): String = {
    if (Zoo.isZooOpen) {
      val client = Zoo.client
      client.create().withMode(CreateMode.EPHEMERAL).withACL(Ids.OPEN_ACL_UNSAFE)
        .forPath("/services/runtime/" + clientDesc)
      new String(client.getData().forPath("/services/configuration"))
    } else "Default configuration"
  }
  def registerInZoo(): Unit = conf
}
case class Configuration(port: Int, timeout: Duration ,comment: String)
