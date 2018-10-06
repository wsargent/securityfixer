package com.tersesystems.securityfixer.agent;

import net.bytebuddy.asm.Advice;

public class MySystemInterceptor {
    public static SecurityManager securityManager;

    @Advice.OnMethodEnter()
    public static void setSecurityManager(SecurityManager s) {
        System.out.println("Security Manager Input is " + s);
        if (securityManager != null) {
            throw new IllegalStateException("SecurityManager cannot be reset!");
        }
        securityManager = s;
    }
}

