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

import akka.actor.Props
import akka.contrib.pattern.ClusterSingletonProxy
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.routing.{ HttpServiceActor, Route }

object HttpService {

  def props(hostname: String, port: Int): Props =
    Props(new HttpService(hostname, port))
}

class HttpService(hostname: String, port: Int) extends HttpServiceActor with SprayJsonSupport with SettingsActor {

  import context.dispatcher
  import settings.httpService.askTimeout

  val userServicve =
    context.actorOf(ClusterSingletonProxy.props("/user/singleton/user-service", Some("user-service")), "user-service")

  override def preStart(): Unit =
    IO(Http)(context.system) ! Http.Bind(self, hostname, port)

  override def receive: Receive =
    runRoute(apiRoute)

  def apiRoute: Route =
    // format: OFF
    pathPrefix("api") {
      path("users") {
        import UserService._
        get {
          complete {
            (userServicve ? GetUsers).mapTo[GetUsersResponse.Users]
          }
        } ~
        post {
          entity(as[SignUp]) { signUp =>
            complete {
              (userServicve ? signUp).mapTo[SignUpResponse] map {
                case SignUpResponse.NameTaken(name) => StatusCodes.Conflict
                case SignUpResponse.SignedUp(name)  => StatusCodes.Created
              }
            }
          }
        }
      }
    } // format: ON
}
