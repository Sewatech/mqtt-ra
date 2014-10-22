/**
 * Copyright 2014 Sewatech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.sewatech.mqttra.example;

import fr.sewatech.mqttra.api.MqttConnectionFactory;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;

public class MqttResourceProducer {

  @Produces @Resource(name = "mqtt/AnswerCF")
  private MqttConnectionFactory answerConnectionFactory;

}
