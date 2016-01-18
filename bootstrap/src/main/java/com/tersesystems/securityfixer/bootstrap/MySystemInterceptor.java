package com.tersesystems.securityfixer.bootstrap;

/**
 * Intercepts the System.setSecurityManager method.
 */
public class MySystemInterceptor {

    private static SecurityManager securityManager;

    public static void setSecurityManager(SecurityManager s) {
        if (securityManager != null) {
            throw new IllegalStateException("SecurityManager cannot be reset!");
        }
        securityManager = s;
    }
}
