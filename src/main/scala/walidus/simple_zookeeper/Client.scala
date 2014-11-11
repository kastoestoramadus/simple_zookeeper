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
import scala.collection.immutable.Map
import org.apache.zookeeper.AsyncCallback.StatCallback
import org.apache.zookeeper.KeeperException.Code

class Client extends Watcher with Runnable {
  val zk = new ZooKeeper("127.0.0.1:2181", 3000, this)

  val clientDesc = InetAddress.getLocalHost().getCanonicalHostName() + '_' + Client.getCounter()

  (1 to 3 toList).takeWhile(_ => zk.getState() == ZooKeeper.States.CONNECTING).map(_ => Thread.sleep(500))
  val conf = zk.getState() match {
    case ZooKeeper.States.CONNECTED => registerAndGetConf
    case s                          => println("Unknown state: " + s); "Default configuration on port 707"
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
      print(" >" + clientDesc + " is alive!")
    }
    println("###>" + clientDesc + " is terminated.")
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
          // no watchs was set so below method won't no be fired?
          zk.close();
          println("Connection with zookeeper lost: from " + clientDesc)
        }
      }
    }
  }
}
/**
 * Class for ilustrating implementation of requirements for recruitment task. TBD with ZK console
 * 0. uruchom zookeepera i clienta do poglądu "drzewa katalogowego"
 * 1. odpalę kilka usług i sprawdzę zawartość /services/runtime. Liczba
 * odpalonych usług będzie odpowiadała liczbie węzłów.
 * 2. odpalę usługę i sprawdzę, czy używana (wyświetlona) konfiguracja
 * odpowiada zapisanej w ZooKeeper z węzła /services/configuration
 * 3. Zamknę ZooKeeper i sprawdzę liczbę uruchomionych usług.
 * 4. Przy zatrzymanym ZooKeeperze, uruchomię poprawnie usługę.
 */
object Client extends Watcher with StatCallback {
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
        case ZooKeeper.States.CONNECTED => zk.exists("/services/runtime/0", true, this, null)
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
  def processResult(rc: Int, path: String, ctx: AnyRef, stat: Stat): Unit = {
    alive = Code.get(rc) match {
      case Code.OK             => true
      case Code.NONODE         => false
      case Code.SESSIONEXPIRED => true
      case Code.NOAUTH         => true
      case _ => {
        // Retry errors
        zk.exists("/services/runtime/0", true, this, null);
        false
      }
    }

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