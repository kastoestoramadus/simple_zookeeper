package walidus.simple_zookeeper;

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.test.BaseClassForTests
import org.scalatest.Assertions
import org.scalatest.testng.TestNGSuiteLike
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertNull
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import walidus.simple_zookeeper.KeptByZoo
import walidus.simple_zookeeper.Zoo
import walidus.simple_zookeeper.Configuration
import spray.json._
import DefaultJsonProtocol._

// I am forced to use TestNg, BaseClassForTests uses it
class ZooSpec extends BaseClassForTests with TestNGSuiteLike with SomethingDoer with Assertions {

  var cli: CuratorFramework = null

  //def cli_=(cf: org.apache.curator.framework.CuratorFramework): Unit = ???
  //def zkTestServer_=(ts: org.apache.curator.test.TestingServer): Unit = ???

  @BeforeMethod
  override def setup(): Unit = {
    super.setup
    cli = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(1000));
    Zoo.initZoo(cli)
    registerInZoo()
    //cli.start()
  }

  @AfterMethod
  override def teardown(): Unit = {
    super.teardown
    if (cli.getState() != CuratorFrameworkState.STOPPED) cli.close();
  }

  @Test
  def shouldInitializeProperly(): Unit = {
    assertNotNull(cli.checkExists().forPath("/services"))
    assertNotNull(cli.checkExists().forPath("/services/runtime"))
    assertNotNull(cli.checkExists().forPath("/services/configuration"))
  }
  @Test
  def shouldRegisterProperly(): Unit = {
    assertNotNull(cli.checkExists().forPath("/services/runtime/" + clientDesc),
        "Conf is: "+conf+" Children: "+cli.getChildren().forPath("/services/runtime").toString)
    assertNull(cli.checkExists().forPath("/services/notexisted"))
  }
  @Test
  def shouldLoadConfigurationFromZooWithourConnection(): Unit = {
    
    conf match {
      case Configuration("You are from Zoo.", 7, 2000L) => // it is ok
      case _ => assert(false)
    }
  }
  // Can I belive this test..
  @Test
  def shouldRunWhenNoConnection(): Unit = {
    server.close()
    cli.close()
    assertEquals("COMPLETE", doSomething)
  }
  // Can I belive this test..
  @Test
  def shouldFireWhenNoConnection(): Unit = {
    server.close()
    cli.close()
    val doer = new AnyRef with SomethingDoer
    doer.registerInZoo()
    assertEquals("COMPLETE", doer.doSomething)
  }
  @Test
  def shouldLoadDefaultConfigurationWithourConnection(): Unit = {
    server.close()
    cli.close()
    val doer = new AnyRef with SomethingDoer
    doer.registerInZoo()
    assertEquals(Zoo.defaultConfiguration, doer.conf)
  }
}

trait SomethingDoer extends KeptByZoo {
  val serviceName = "testService"

  def doSomething: String = { Thread.sleep(100); "COMPLETE" }
}
