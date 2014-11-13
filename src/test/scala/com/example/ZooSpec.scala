
import org.specs2.mutable.Specification
import walidus.simple_zookeeper.KeptByZoo
import spray.testkit.Specs2RouteTest
import walidus.simple_zookeeper.MyService
import org.apache.curator.test.BaseClassForTests
import org.apache.curator.test.TestingServer
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.framework.CuratorFramework
import walidus.simple_zookeeper.Zoo
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert

class ZooSpec extends BaseClassForTests with KeptByZoo {
  val serviceName = "testService"

  var zkTestServer: TestingServer = null
  var cli: CuratorFramework = null

  //def cli_=(cf: org.apache.curator.framework.CuratorFramework): Unit = ???
  //def zkTestServer_=(ts: org.apache.curator.test.TestingServer): Unit = ???

  @Before
  def startZookeeper(): Unit = {
    zkTestServer = new TestingServer(2181);
    cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(), new RetryOneTime(2000));
  }

  @After
  def stopZookeeper(): Unit = {
    cli.close();
    zkTestServer.stop();
  }
  @Test
  def shouldInitializeProperly(): Unit = {
    Zoo.initDirStructureAndConfiguration
    Assert.assertNotNull(cli.checkExists().forPath("/services"))
    Assert.assertNotNull(cli.checkExists().forPath("/services/runtime"))
    Assert.assertNotNull(cli.checkExists().forPath("/services/configuration"))
    Assert.assertNull(cli.checkExists().forPath("/services"))
  }
  
  
  /*
  "Zoo" should {

    "initialize properly" in {
      true
    }
  }
  "KeptByZoo" should {

    "Create ephemeric placeholder" in {
      true
    }
  }
  "KeptByZoo" should {

    "Continue to run in case of connection lost" in {
      true
    }
  }
  "KeptByZoo" should {

    "Fire witout ZooKeeper connection" in {
      true
    }
  }
  */
}
