package com.allandequeiroz.playground

import scala.collection.JavaConverters._

class ScenarioBase(
                    private var concurrentUsers: Int,
                    private var headers: java.util.Map[String, String]) {

  def getConcurrentUsers = concurrentUsers

  def setConcurrentUsers(_concurrentUsers: Int): Unit = {
    concurrentUsers = _concurrentUsers
  }

  def getHeaders(): Map[String, String] = {
    if(headers == null){
      return null;
    }
    val mutableHeaders = headers.asScala
    val immutableHeaders = Map() ++ mutableHeaders
    return immutableHeaders
  }

  def setHeaders(_headers: java.util.Map[String, String]): Unit = {
    headers = _headers
  }
}
