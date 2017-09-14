package com.alefeducation

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import dispatch.{Http, url}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import play.api.libs.json.{Format, JsError, JsSuccess, Json}

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

  case class SimpleNestedClass(field1: String)
  case class SimpleCaseClass(field1: String, field2: Int, field3: Boolean, nestedClass: SimpleNestedClass)

  implicit val simpleNestedClassFormat  = Json.format[SimpleNestedClass]
  implicit val simpleCaseClassFormat    = Json.format[SimpleCaseClass]

  "A json as string" should "be converted to a case class" in {

    Json.parse(
      """
        |{
        |  "field1" : "field1Value",
        |  "field2" : 1,
        |  "field3" : true,
        |  "nestedClass": {
        |     "field1" : "nestedClassField1Value"
        |  }
        |}
      """.stripMargin).validate[SimpleCaseClass] match {
        case JsSuccess(simpleCaseClass, _) => s"Json parsed as: $simpleCaseClass"
        case JsError(errors) => s"Failed to parse json due to: $errors"
    }

  }

  "A json" should "be converted to a string representation" in {

    val expected =
      """{
        |  "field1" : "field1Value",
        |  "field2" : 1,
        |  "field3" : true,
        |  "nestedClass" : {
        |    "field1" : "nestedClassField1Value"
        |  }
        |}""".stripMargin

    val simpleClassAsJson = Json.toJson[SimpleCaseClass](SimpleCaseClass("field1Value", 1, true, SimpleNestedClass("nestedClassField1Value")))
    Json.prettyPrint(simpleClassAsJson) should equal(expected)

  }

  case class ClassWithHtml(html: String)

  implicit val classWithHtmlFormat = Json.format[ClassWithHtml]

  "A json with html" should "be properly converted to a string representation with escaped characters" in {

    val classWithHtml = ClassWithHtml(
      """
        |<!DOCTYPE html>
        |<html>
        |<head>
        |    <meta charset="utf-8">
        |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
        |    <title>SC6_MLO_007_DOK1</title>
        |    <link rel="stylesheet" href="../../../assets/bootstrap/css/bootstrap.min.css">
        |    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Open+Sans">
        |    <link rel="stylesheet" href="../../../assets/css/styles.css">
        |</head>
      """.stripMargin)

    val expectedJsonAsString =
      """{
        |  "html" : "\n<!DOCTYPE html>\n<html>\n<head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>SC6_MLO_007_DOK1</title>\n    <link rel=\"stylesheet\" href=\"../../../assets/bootstrap/css/bootstrap.min.css\">\n    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Open+Sans\">\n    <link rel=\"stylesheet\" href=\"../../../assets/css/styles.css\">\n</head>\n      "
        |}""".stripMargin

    val classWithHtmlAsJson = Json.toJson[ClassWithHtml](classWithHtml)
    Json.prettyPrint(classWithHtmlAsJson) should equal(expectedJsonAsString)

  }

  case class KeyValue(key: String)

  implicit val keyValueFormats = Json.format[KeyValue]

  "WireMock stub" should "return response with json in body and with status OK" in {
    val path = "/my/resource"
    stubFor(post(urlEqualTo(path))
      .withRequestBody(equalToJson(""" { "key": "value" } """))
      .willReturn(
        aResponse()
          .withBody("OK")
          .withStatus(200)))
    val request = url(s"http://$Host:$Port$path")
      .setBody(Json.toJson[KeyValue](KeyValue("value")).toString)
      .POST
    val responseFuture = Http.default(request)

    val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
    response.getStatusCode should be(200)
    response.getResponseBody should equal("OK")
  }

  "WireMock stub" should "receive a json in body and return OK" in {

    val classWithHtml = ClassWithHtml(
      """
        |<!DOCTYPE html>
        |<html>
        |<head>
        |    <meta charset="utf-8">
        |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
        |    <title>SC6_MLO_007_DOK1</title>
        |    <link rel="stylesheet" href="../../../assets/bootstrap/css/bootstrap.min.css">
        |    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Open+Sans">
        |    <link rel="stylesheet" href="../../../assets/css/styles.css">
        |</head>
      """.stripMargin)

    val expectedJsonAsString =
      """{
        |  "html" : "\n<!DOCTYPE html>\n<html>\n<head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>SC6_MLO_007_DOK1</title>\n    <link rel=\"stylesheet\" href=\"../../../assets/bootstrap/css/bootstrap.min.css\">\n    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Open+Sans\">\n    <link rel=\"stylesheet\" href=\"../../../assets/css/styles.css\">\n</head>\n      "
        |}""".stripMargin

    val path = "/my/resource"
    stubFor(post(urlEqualTo(path))
      .withRequestBody(equalToJson(expectedJsonAsString))
      .willReturn(
        aResponse()
          .withBody("OK")
          .withStatus(200)))
    val request = url(s"http://$Host:$Port$path")
      .setBody(Json.toJson[ClassWithHtml](classWithHtml).toString)
      .POST
    val responseFuture = Http.default(request)

    val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
    response.getStatusCode should be(200)
    response.getResponseBody should equal("OK")
  }


}