package securityfixer;

import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;

public class Main {

    static class SillyPolicy extends Policy {
        @Override
        public boolean implies(ProtectionDomain domain, Permission permission) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        Policy.setPolicy(new SillyPolicy());
        System.setSecurityManager(new SecurityManager());

        System.out.println("Security manager is set!");
        try {
            System.setSecurityManager(null);
            System.err.println("Security manager was reset!");
        } catch (SecurityException e) {
            System.out.println(e.getMessage() + " -- Security manager cannot be reset!");
        }

    }

}
