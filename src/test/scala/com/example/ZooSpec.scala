
import org.specs2.mutable.Specification
import walidus.simple_zookeeper.KeptByZoo
import walidus.simple_zookeeper.Zoo
import spray.testkit.Specs2RouteTest
import walidus.simple_zookeeper.MyService
import org.apache.curator.test.BaseClassForTests
import org.apache.curator.test.TestingServer
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.framework.CuratorFramework
import walidus.simple_zookeeper.Zoo
import org.apache.curator.framework.CuratorFrameworkFactory
import org.testng.Assert._
import org.testng.annotations.Test
import org.testng.annotations.Configuration
import org.scalatest.Assertions
import org.scalatest.testng.TestNGSuiteLike
import org.testng.annotations.BeforeMethod
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeSuite
import org.apache.curator.framework.imps.CuratorFrameworkState;
import walidus.simple_zookeeper.KeptByZoo

class ZooSpec extends BaseClassForTests with TestNGSuiteLike with SomethingDoer with Assertions {

  var cli: CuratorFramework = null

  //def cli_=(cf: org.apache.curator.framework.CuratorFramework): Unit = ???
  //def zkTestServer_=(ts: org.apache.curator.test.TestingServer): Unit = ???


  @BeforeMethod
  override def setup(): Unit = {
    super.setup
    cli = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(2000));
    Zoo.initZoo(cli)
    //cli.start()
  }

  @AfterMethod
  override def teardown(): Unit = {
    super.teardown
    if(cli.getState() != CuratorFrameworkState.STOPPED) cli.close();
  }

  @Test
  def shouldInitializeProperly(): Unit = {
    assertNotNull(cli.checkExists().forPath("/services"))
    assertNotNull(cli.checkExists().forPath("/services/runtime"))
    assertNotNull(cli.checkExists().forPath("/services/configuration"))
  }
  @Test
  def shouldRegisterProperly(): Unit = {
    registerInZoo()
    assertNotNull(cli.checkExists().forPath("/services/runtime/"+clientDesc))
    assertNull(cli.checkExists().forPath("/services/notexisted"))
  }
  
  @Test
  def shouldRunWhenNoConnection(): Unit = {
    server.close()
    cli.close()
    assertEquals("COMPLETE", doSomething)
  }
  @Test
  def shouldFireWhenNoConnection(): Unit = {
    server.close()
    cli.close()
    val doer = new AnyRef with SomethingDoer
    doer.registerInZoo()
    assertEquals("COMPLETE", doer.doSomething)
  }
}
trait SomethingDoer extends KeptByZoo {
  val serviceName = "testService"

  def doSomething: String = {Thread.sleep(100); "COMPLETE"}
}
