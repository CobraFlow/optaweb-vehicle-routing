package org.optaweb.vehiclerouting.plugin.security.aop;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.optaweb.vehiclerouting.plugin.persistence.TenantCrudRepository;
import org.optaweb.vehiclerouting.plugin.persistence.TenantEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private final TenantCrudRepository tenantRepository;

    public TenantInterceptor(TenantCrudRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Around("@annotation(InboundRequest)")
    public Object logInboundRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Intercepting inbound request...");

        log.debug("Extracting tenant id from method arguments!");
        String tenantName = extractTenantName()
                .orElseThrow(RuntimeException::new);

        log.debug("Finding tenant by name!");
        TenantEntity tenant = tenantRepository
                .findByName(tenantName)
                .orElseThrow(RuntimeException::new);

        log.debug("Setting current tenant to Thread local variable!");
        TenantContext.setCurrentTenant(tenant);

        try {
            log.debug("Continuing with the execution!");
            return joinPoint.proceed();
        } finally {
            log.debug(" Clearing tenant context!");
            TenantContext.clear();
        }
    }

    private Optional<String> extractTenantName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken keycloakToken = (KeycloakAuthenticationToken) authentication;
            Principal principal = (Principal) keycloakToken.getPrincipal();
            if (principal instanceof KeycloakPrincipal) {
                KeycloakPrincipal<? extends KeycloakSecurityContext> keycloakPrincipal =
                        (KeycloakPrincipal<? extends KeycloakSecurityContext>) principal;
                Map<String, Object> otherClaims = keycloakPrincipal.getKeycloakSecurityContext().getToken().getOtherClaims();
                if (otherClaims.containsKey("tenant")) {
                    return of((String) otherClaims.get("tenant"));
                }
            }
        }
        return empty();
    }
}
