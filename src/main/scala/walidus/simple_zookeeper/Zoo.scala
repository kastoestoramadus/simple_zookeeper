package walidus.simple_zookeeper

import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.CreateMode
import java.net.InetAddress
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.imps.CuratorFrameworkState
import java.util.concurrent.TimeUnit
import org.apache.curator.RetryPolicy
import org.apache.curator.retry.RetryOneTime
import java.net.Socket
import java.net.InetSocketAddress

object Zoo {
  def connectionHost: String = "127.0.0.1"
  def connectionPort: Int = 2181
  val client = {
    val retryPolicy: RetryPolicy = new RetryOneTime(300)
    CuratorFrameworkFactory.builder().connectString(connectionHost + ':' + connectionPort)
      .retryPolicy(retryPolicy).build()
  }
  if (serverListening(connectionHost, connectionPort)) client.start()

  def initDirStructureAndConfiguration(): Unit = {
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
  def createZooKeeperClient() = client.getZookeeperClient()
  def isZooOpen(): Boolean =
    client.getState() == CuratorFrameworkState.STARTED &&
      serverListening(connectionHost, connectionPort)
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
  val conf = getConf

  def getConf(): String = {
    if (Zoo.isZooOpen) {
      val client = Zoo.createZooKeeperClient()
      client.blockUntilConnectedOrTimedOut()
      val zk = client.getZooKeeper()
      def registerAndGetConf() = {
        zk.create("/services/runtime/" + clientDesc, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        val rString = new String(zk.getData("/services/configuration", false, null))
        zk.close
        rString
      }
      registerAndGetConf
    } else "Default configuration on port 808"
  }
}
