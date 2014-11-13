package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import walidus.simple_zookeeper.MyService

class ServicesSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system

  "MyService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> homeRoute ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> homeRoute ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(homeRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
    "return pong for ping GET requests" in {
      Get("/ping") ~> pingRoute ~> check {
        responseAs[String] must contain("pong")
      }
    }
  }
  "return !? for pong GET requests" in {
    Get("/pong") ~> pongRoute ~> check {
      responseAs[String] must contain("!?")
    }
  }

}
