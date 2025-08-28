package org.gft.gbt.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {
    
    public boolean hasCustomerId(Authentication authentication, String customerId) {
        String currentUsername = authentication.getName();
        // For demo purposes, we're checking if the username matches the customerId
        // In a real application, you would check if the current user has access to the given customerId
        return currentUsername.equals(customerId);
    }
}
