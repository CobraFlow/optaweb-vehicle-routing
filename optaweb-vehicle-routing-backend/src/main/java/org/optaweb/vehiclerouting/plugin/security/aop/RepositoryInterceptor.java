package org.optaweb.vehiclerouting.plugin.security.aop;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.optaweb.vehiclerouting.plugin.persistence.TenantCrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryInterceptor {

    private final Logger logger = LoggerFactory.getLogger(RepositoryInterceptor.class);
    private final EntityManager entityManager;
    private final Environment env;

    public RepositoryInterceptor(EntityManager entityManager, Environment env) {
        this.entityManager = entityManager;
        this.env = env;
    }

    @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
    public Object inWebLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean isClassTypeTenantRepository = ofAClassTypeTenantRepository(joinPoint.getSignature().getDeclaringType());
        if (isProd()) {
            if (entityManager.isOpen() && !isClassTypeTenantRepository) {
                Session session = entityManager.unwrap(Session.class);
                Integer id = TenantContext.getCurrentTenantId();
                session.enableFilter("distanceFilter").setParameter("tenantId", id);
                session.enableFilter("locationFilter").setParameter("tenantId", id);
                session.enableFilter("vehicleFilter").setParameter("tenantId", id);
            }
        } else {
            if (entityManager.isJoinedToTransaction() && !isClassTypeTenantRepository) {
                Session session = entityManager.unwrap(Session.class);
                Integer id = TenantContext.getCurrentTenantId();
                session.enableFilter("distanceFilter").setParameter("tenantId", id);
                session.enableFilter("locationFilter").setParameter("tenantId", id);
                session.enableFilter("vehicleFilter").setParameter("tenantId", id);
            }
        }
        return joinPoint.proceed();
    }

    private boolean isProd() {
        return Arrays.asList(env.getActiveProfiles()).contains("prod");
    }

    private boolean ofAClassTypeTenantRepository(Class<?> declaringType) {
        return declaringType.equals(TenantCrudRepository.class);
    }
}
