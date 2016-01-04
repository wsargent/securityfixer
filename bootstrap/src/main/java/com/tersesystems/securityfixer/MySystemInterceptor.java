package com.tersesystems.securityfixer;

public class MySystemInterceptor {

    public static void setSecurityManager(SecurityManager s) {
        throw new IllegalStateException("hello");
    }
}
