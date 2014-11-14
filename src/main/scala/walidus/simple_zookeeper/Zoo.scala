package walidus.simple_zookeeper

import java.net.InetAddress
import java.net.Socket

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.zookeeper.CreateMode

import MyJsonProtocol.configurationFormat
import spray.json.pimpString

object Zoo {

  private var curatorSingleton: Option[CuratorFramework] = None

  def client = curatorSingleton.getOrElse(throw new IllegalArgumentException("Zoo not initialized"))

  def initZoo(c: CuratorFramework): Unit = {

    if (curatorSingleton != None && client.getState() == CuratorFrameworkState.STARTED)
      throw new IllegalArgumentException("Zoo can be initialized only once")

    curatorSingleton = Some(c)
    if (serverListening(connectionHost, connectionPort)) {
      client.start()

      if (client.checkExists().forPath("/services") == null)
        client.create().forPath("/services")
      if (client.checkExists().forPath("/services/runtime") == null)
        client.create().forPath("/services/runtime")
      if (client.checkExists().forPath("/services/configuration") == null)
        client.create()
          .forPath("/services/configuration",
            """{"comment":"You are from Zoo.","port":7,"timeout":2000}""".getBytes())
    }
  }
  def isZooOpen(): Boolean =
    serverListening(connectionHost, connectionPort) &&
      client.getState() == CuratorFrameworkState.STARTED

  def connectString = client.getZookeeperClient().getCurrentConnectionString()
  def connectionHost: String = connectString.split(':')(0)
  def connectionPort: Int = Integer.parseInt(connectString.split(':')(1))
  def defaultConfiguration = Configuration("Default configuration", 808, 3000L)
  // method below is a shame that curator do not provide
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
  val serviceName: String // watch out, should have value during initialization
  val clientDesc = serviceName + '_' + InetAddress.getLocalHost().getCanonicalHostName()
  lazy val conf: Configuration = registerAndGetConf

  private def registerAndGetConf(): Configuration = {
    if (Zoo.isZooOpen) {
      import MyJsonProtocol._
      Zoo.client.create().withMode(CreateMode.EPHEMERAL)
        .forPath("/services/runtime/" + clientDesc)
      new String(Zoo.client.getData()
        .forPath("/services/configuration"))
        .parseJson.convertTo[Configuration]
    } else Configuration("Default configuration", 808, 3000L)
  }
  def registerInZoo(): Unit = conf // depending on lazyness of conf
}

