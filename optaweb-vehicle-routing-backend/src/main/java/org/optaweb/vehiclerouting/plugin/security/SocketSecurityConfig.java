package org.optaweb.vehiclerouting.plugin.security;

import static org.springframework.messaging.simp.SimpMessageType.*;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@Profile("keycloak")
public class SocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpTypeMatchers(CONNECT, SUBSCRIBE, MESSAGE).authenticated()
                .simpDestMatchers("/**").authenticated()
                .anyMessage().authenticated();
    }

    @PostConstruct
    public void enableAuthContextOnSpawnedThreads() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}