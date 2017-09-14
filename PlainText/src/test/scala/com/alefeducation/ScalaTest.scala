package com.alefeducation

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import dispatch.{Http, url}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class ScalaTest extends FlatSpec with Matchers with BeforeAndAfterEach {

  val Port = 8080
  val Host = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(Port))

  override def beforeEach {
    wireMockServer.start()
    WireMock.configureFor(Host, Port)
  }

  override def afterEach {
    wireMockServer.stop()
  }

  "WireMock stub" should "return an empty response with status OK" in {
    val path = "/my/resource"
    stubFor(get(urlEqualTo(path))
      .willReturn(
        aResponse()
          .withStatus(200)))
    val request = url(s"http://$Host:$Port$path").GET
    val responseFuture = Http.default(request)

    val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
    response.getStatusCode should be(200)
  }

  "WireMock stub" should "return response with json in body and with status OK" in {
    val path = "/my/resource"
    stubFor(post(urlEqualTo(path))
      .withRequestBody(equalToJson(""" { "key": "value" } """))
      .willReturn(
        aResponse()
          .withBody("OK")
          .withStatus(200)))
    val request = url(s"http://$Host:$Port$path")
      .setBody(""" { "key": "value" } """)
      .POST
    val responseFuture = Http.default(request)

    val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
    response.getStatusCode should be(200)
    response.getResponseBody should equal("OK")
  }

}