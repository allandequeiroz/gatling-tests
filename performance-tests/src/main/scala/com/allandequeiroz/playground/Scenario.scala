package com.allandequeiroz.playground

import java.util

class Scenario(private var baseUrl: String,
               private var name: String,
               private var auth: Auth,
               private var requests: util.List[Request]) {

  def getBaseUrl = baseUrl

  def setBaseUrl(_baseUrl: String): Unit = {
    baseUrl = _baseUrl
  }

  def getName = name

  def setName(_name: String): Unit = {
    name = _name
  }

  def getAuth = auth

  def setUrl(_auth: Auth): Unit = {
    auth = _auth
  }

  def getRequests = requests

  def setRequests(_requests: util.List[Request]): Unit = {
    requests = _requests
  }
}
