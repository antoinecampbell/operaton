/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.model.bpmn;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.model.bpmn.instance.*;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Menski
 */
class CollaborationParserTest {

  private static BpmnModelInstance modelInstance;
  private static Collaboration collaboration;

  @BeforeAll
  static void parseModel() {
    modelInstance = Bpmn.readModelFromStream(CollaborationParserTest.class.getResourceAsStream("CollaborationParserTest.bpmn"));
    collaboration = modelInstance.getModelElementById("collaboration1");
  }

  @Test
  void testConversations() {
    assertThat(collaboration.getConversationNodes()).hasSize(1);

    ConversationNode conversationNode = collaboration.getConversationNodes().iterator().next();
    assertThat(conversationNode).isInstanceOf(Conversation.class);
    assertThat(conversationNode.getParticipants()).isEmpty();
    assertThat(conversationNode.getCorrelationKeys()).isEmpty();
    assertThat(conversationNode.getMessageFlows()).isEmpty();
  }

  @Test
  void testConversationLink() {
    Collection<ConversationLink> conversationLinks = collaboration.getConversationLinks();
    for (ConversationLink conversationLink : conversationLinks) {
      assertThat(conversationLink.getId()).startsWith("conversationLink");
      assertThat(conversationLink.getSource()).isInstanceOf(Participant.class);
      Participant source = (Participant) conversationLink.getSource();
      assertThat(source.getName()).isEqualTo("Pool");
      assertThat(source.getId()).startsWith("participant");

      assertThat(conversationLink.getTarget()).isInstanceOf(Conversation.class);
      Conversation target = (Conversation) conversationLink.getTarget();
      assertThat(target.getId()).isEqualTo("conversation1");
    }
  }

  @Test
  void testMessageFlow() {
    Collection<MessageFlow> messageFlows = collaboration.getMessageFlows();
    for (MessageFlow messageFlow : messageFlows) {
      assertThat(messageFlow.getId()).startsWith("messageFlow");
      assertThat(messageFlow.getSource()).isInstanceOf(ServiceTask.class);
      assertThat(messageFlow.getTarget()).isInstanceOf(Event.class);
    }
  }

  @Test
  void testParticipant() {
    Collection<Participant> participants = collaboration.getParticipants();
    for (Participant participant : participants) {
      assertThat(participant.getProcess().getId()).startsWith("process");
    }
  }

  @Test
  void testUnused() {
    assertThat(collaboration.getCorrelationKeys()).isEmpty();
    assertThat(collaboration.getArtifacts()).isEmpty();
    assertThat(collaboration.getConversationAssociations()).isEmpty();
    assertThat(collaboration.getMessageFlowAssociations()).isEmpty();
    assertThat(collaboration.getParticipantAssociations()).isEmpty();
  }


  @AfterAll
  static void validateModel() {
    Bpmn.validateModel(modelInstance);
  }

}
