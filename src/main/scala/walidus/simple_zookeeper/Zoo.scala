package walidus.simple_zookeeper

import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.Watcher.Event
import java.net.InetAddress

object Zoo extends Watcher{
  
  // is it right place for init method..
  def initDirStructureAndConfiguration(): Unit = {
    val zk = new ZooKeeper("127.0.0.1:2181", 3000, this)
    if (zk.exists("/services", false) == null)
      zk.create("/services", new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    if (zk.exists("/services/runtime", false) == null)
      zk.create("/services/runtime", new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    if (zk.exists("/services/configuration", false) == null)
      zk.create("/services/configuration", "Client configuration with port 7".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
    zk.close()
  }

  @Override
  def process(event: WatchedEvent): Unit = {
// ignore, TODO can we give watcher as null?
  }
}
trait KeptByZoo extends Watcher {
  val serviceName: String
  val clientDesc = serviceName + '_' + InetAddress.getLocalHost().getCanonicalHostName()
  val zk = new ZooKeeper("127.0.0.1:2181", 3000, KeptByZoo.this)

  def getConf(): String = {
    (1 to 3 toList)
      .takeWhile(_ => zk.getState() == ZooKeeper.States.CONNECTING)
      .foreach(_ => Thread.sleep(500)) // curator would help for this waiting blockUntilConnectedOrTimedOut()
    zk.getState() match {
      case ZooKeeper.States.CONNECTED => registerAndGetConf
      case s                          => println("Unknown state: " + s); "Default configuration on port 808"
    }
  }

  private def registerAndGetConf() = {
    zk.create("/services/runtime/" + clientDesc, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    new String(zk.getData("/services/configuration", false, null))
  }

  @Override
  def process(event: WatchedEvent): Unit = {
    val path = event.getPath();
    if (event.getType() == Event.EventType.None) {
      // We are are being told that the state of the
      // connection has changed
      event.getState() match {
        case KeeperState.SyncConnected =>
        case KeeperState.Disconnected => {

          zk.close() // what else to do ?
        }
        case _ => // should not happen
      }
    }
  }
}
