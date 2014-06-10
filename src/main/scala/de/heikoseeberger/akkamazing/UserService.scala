/*
 * Copyright 2014 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.akkamazing

import akka.actor.{ Actor, ActorLogging, Props }
import spray.json.DefaultJsonProtocol
import akka.persistence.EventsourcedProcessor

object UserService {

  case object GetUsers

  object Users extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class Users(names: Set[String])

  object SignUp extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class SignUp(name: String)

  case class SignedUp(name: String)

  case class NameTaken(name: String)

  def props: Props =
    Props(new UserService)
}

class UserService extends EventsourcedProcessor with ActorLogging {

  import UserService._

  var names = Set.empty[String]

  override def receiveCommand: Receive = {
    case GetUsers                            => sender() ! Users(names)
    case SignUp(name) if names contains name => sender() ! NameTaken(name)
    case SignUp(name)                        => persist(SignedUp(name))(handleSignedUpThenRespond)
  }

  override def receiveRecover: Receive = {
    case signedUp: SignedUp => handleSignedUp(signedUp)
  }

  def handleSignedUpThenRespond(signedUp: SignedUp): Unit = {
    handleSignedUp(signedUp)
    sender() ! signedUp
  }

  def handleSignedUp(signedUp: SignedUp): Unit = {
    val SignedUp(name) = signedUp
    log.info("Signing up {}", name)
    names += name
  }
}