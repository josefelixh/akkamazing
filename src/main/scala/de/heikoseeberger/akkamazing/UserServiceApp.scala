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

import akka.actor.{ ActorSystem, PoisonPill }
import akka.contrib.pattern.ClusterSingletonManager

object UserServiceApp extends BaseApp {

  override def run(system: ActorSystem, opts: Map[String, String]): Unit = {
    system.actorOf(SharedJournalSetter.props, "shared-journal-setter")
    system.actorOf(
      ClusterSingletonManager.props(UserService.props, "user-service", PoisonPill, Some("user-service")),
      "singleton"
    )
  }
}
