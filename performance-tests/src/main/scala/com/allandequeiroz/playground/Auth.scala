package com.allandequeiroz.playground

class Auth(
            private var username: String,
            private var password: String) {

  def getUsername = username

  def setUsername(_username: String): Unit = {
    username = _username
  }

  def getPassword = password

  def setPassword(_password: String): Unit = {
    username = _password
  }
}
