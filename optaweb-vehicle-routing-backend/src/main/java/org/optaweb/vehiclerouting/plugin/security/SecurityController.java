package org.optaweb.vehiclerouting.plugin.security;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityController {

    @GetMapping(value = "/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        KeycloakSecurityContext ctx = getKeycloakSecurityContext(request);
        if (ctx != null) {
            request.logout();
        }
        return "redirect:/";
    }

    /**
     * The KeycloakSecurityContext provides access to several pieces of information
     * contained in the security token, such as user profile information.
     */
    private KeycloakSecurityContext getKeycloakSecurityContext(HttpServletRequest request) {
        return (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
    }
}
