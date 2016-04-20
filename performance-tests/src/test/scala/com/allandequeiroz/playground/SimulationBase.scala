package com.allandequeiroz.playground

import com.google.gson.Gson
import io.gatling.core.Predef._
import io.gatling.core.structure.{PopulatedScenarioBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.config.HttpProtocolBuilder
import org.apache.commons.io.IOUtils

import scala.collection.mutable.ListBuffer

class SimulationBase {

  def getPopulatedScenarioBuilder(newScenarioFile: String): ListBuffer[PopulatedScenarioBuilder] = {

    val classLoader = getClass.getClassLoader
    val json = new Gson()

    val scenarioBase = getScenarioBase(newScenarioFile, classLoader, json)
    val newScenario = getNewScenario(newScenarioFile, classLoader, json)
    val httpConf = getHttpConf(newScenario, scenarioBase)

    var populatedScenarioBuilderList = new ListBuffer[PopulatedScenarioBuilder]()
    var scenarioBuilder = scenario(newScenario.getName).exec();
    var scenarioRequests = newScenario.getRequests.iterator()

    while (scenarioRequests.hasNext) {

      var scenarioRequest = scenarioRequests.next
      var scenarioAuth = newScenario.getAuth

      if(scenarioAuth != null){

        scenarioBuilder = getScenarioBuilderWithAuth(scenarioBuilder, scenarioRequest, scenarioAuth)
      }else{

        scenarioBuilder = getScenarioBuilderWithoutAuth(scenarioBuilder, scenarioRequest)

      }
    }

    var populatedScenarioBuilder = scenarioBuilder.inject(atOnceUsers(scenarioBase.getConcurrentUsers)).protocols(httpConf);
    populatedScenarioBuilderList += populatedScenarioBuilder

    return populatedScenarioBuilderList;
  }

  def getNewScenario(newScenarioFile: String, classLoader: ClassLoader, json: Gson) : Scenario = {
    var newScenarioIS = classLoader.getResourceAsStream(newScenarioFile);
    var newScenarioContent = IOUtils.toString(newScenarioIS);
    return json.fromJson(newScenarioContent, classOf[Scenario])
  }

  def getScenarioBase(newScenarioFile: String, classLoader: ClassLoader, json: Gson) : ScenarioBase = {
    var scenarioBaseIS = classLoader.getResourceAsStream("scenario-base.json");
    var scenarioBaseContent = IOUtils.toString(scenarioBaseIS);
    return json.fromJson(scenarioBaseContent, classOf[ScenarioBase])
  }

  def getHttpConf(newScenario: Scenario, scenarioBase: ScenarioBase): HttpProtocolBuilder = {
    var scenariosHeaders = scenarioBase.getHeaders()
    if(scenariosHeaders == null){

      return http.baseURL(newScenario.getBaseUrl)
    }else{

      return http.baseURL(newScenario.getBaseUrl).headers(scenariosHeaders)
    }
  }

  def getScenarioBuilderWithAuth(scenarioBuilder: ScenarioBuilder, scenarioRequest: Request, scenarioAuth: Auth): ScenarioBuilder ={
    return scenarioBuilder.exec(http(scenarioRequest.getName)
      .get(scenarioRequest.getUrl)
      .basicAuth(scenarioAuth.getUsername,scenarioAuth.getPassword))
      .pause(2)
  }

  def getScenarioBuilderWithoutAuth(scenarioBuilder: ScenarioBuilder, scenarioRequest: Request): ScenarioBuilder ={
    return scenarioBuilder.exec(http(scenarioRequest.getName)
      .get(scenarioRequest.getUrl))
      .pause(2)
  }
}
