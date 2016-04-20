package com.allandequeiroz.playground

class Request(
               private var name: String,
               private var url: String) {

  def getName = name

  def setName(_name: String): Unit = {
    name = _name
  }

  def getUrl = url

  def setUrl(_url: String): Unit = {
    url = _url
  }
}
