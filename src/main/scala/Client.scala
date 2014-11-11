package walidus.simple_zookeeper

import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.data.Stat
import java.net.InetAddress
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.zookeeper.Watcher.Event

class Client extends Watcher with Runnable {
  val zk = new ZooKeeper("127.0.0.1:2181", 3000, this)
  val clientDesc = InetAddress.getLocalHost().getCanonicalHostName() + '_' + Client.getCounter()
  Thread sleep 1000
  val conf = zk.getState() match {
    case ZooKeeper.States.CONNECTED => registerAndGetConf
    case s => println("Unknown state: "+s); "Default configuration on port 707"
  }
  private def registerAndGetConf() = {
          zk.create("/services/runtime/" + clientDesc, new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
      new String(zk.getData("/services/configuration", false, null))
  }

  @Override
  def run(): Unit = {
    println("###########" + conf + " <- config from: " + clientDesc)
    while (Client.alive) {
      Thread sleep 2000
      print(" >"+clientDesc + " is alive!")
    }
    println(clientDesc + " is terminated.")
  }
  @Override
  def process(event: WatchedEvent): Unit = {
    val path = event.getPath();
    if (event.getType() == Event.EventType.None) {
      // We are are being told that the state of the
      // connection has changed
      event.getState() match {
        case KeeperState.SyncConnected =>
        // In this particular example we don't need to do anything
        // here - watches are automatically re-registered with 
        // server and any watches triggered while the client was 
        // disconnected will be delivered (in order of course)
        case KeeperState.Disconnected => {
          // It's all over
          zk.close();
          println("Connevtion with zookeeper lost: from " + clientDesc)
        }
      }
    } else {
      // ignore
    }
  }
}

object Client extends Watcher {
  var counter = 1
  var alive = true

  BasicConfigurator.configure()
  Logger.getRootLogger().setLevel(Level.WARN);

  val zk = new ZooKeeper("127.0.0.1:2181", 3000, this)
  //zk.create("/services", new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
  //zk.create("/services/runtime", new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
  //zk.create("/services/configuration", "Client configuration with port 7".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

  def getCounter() = { val cnt = counter; counter += 1; cnt }

  // program should be terminated by deleting the zero znode
  @Override
  def main(args: Array[String]): Unit = try {

    zk.create("/services/runtime/0", new Array[Byte](0), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)

    new Thread(new Client()).start()
    new Thread(new Client()).start()
    new Thread(new Client()).start()

    while (alive) {
      zk.getState() match {
        case ZooKeeper.States.CONNECTED => zk.exists("/services/runtime/0", false)
        case _                          => //ignore
      }
      Thread sleep 2000
    }
    Thread sleep 2000
    println("<<< Main loop is terminated.>>>")
  } finally {
    alive = false
  }

  @Override
  def process(event: WatchedEvent): Unit = {
    val path = event.getPath();
    if (event.getType() == Event.EventType.None) {
      // We are are being told that the state of the
      // connection has changed
      event.getState() match {
        case KeeperState.SyncConnected =>
        // In this particular example we don't need to do anything
        // here - watches are automatically re-registered with 
        // server and any watches triggered while the client was 
        // disconnected will be delivered (in order of course)
        case KeeperState.Disconnected => {
          // It's all over
          zk.close();
          println("Connection with zookeeper lost: from main")
          new Thread(new Client()).start()
        }
      }
    } else {
      //if (path != null && path.equals(znode)) {
      // Something has changed on the node, let's find out
      //zk.exists(znode, true, this, null);
      //}
    }
  }
  // init configuration file at  /services/configuration
}