/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.vehiclerouting.plugin.websocket;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.optaweb.vehiclerouting.service.error.ErrorMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@SecurityTestExecutionListeners
class WebSocketErrorMessageSenderTest {

    @Test
    @WithMockUser
    void should_send_consumed_message_over_websocket(@Mock SimpMessagingTemplate webSocket) {
        ErrorMessage message = ErrorMessage.of("id", "error");
        new WebSocketErrorMessageSender(webSocket).consumeMessage(message);
        verify(webSocket).convertAndSendToUser(
                eq("user"),
                eq(WebSocketErrorMessageSender.TOPIC_ERROR),
                eq(PortableErrorMessage.fromMessage(message)));
    }
}
