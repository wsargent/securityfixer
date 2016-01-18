package securityfixer;

import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;

/**
 * Runs a program that turns on and then turns off SecurityManager.
 */
public class Main {

    // We don't want any ACTUAL security here when we turn on the security manager...
    static class SillyPolicy extends Policy {
        @Override
        public boolean implies(ProtectionDomain domain, Permission permission) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        // Programmer turns on security manager...
        Policy.setPolicy(new SillyPolicy());
        System.setSecurityManager(new SecurityManager());

        System.out.println("Security manager is set!");
        try {
            // Attacker tries to turn off security manager...
            System.setSecurityManager(null);

            // Happens on normal circumstances...
            System.err.println("ATTACK SUCCEEDED: Security manager was reset!");
        } catch (IllegalStateException e) {
            // Happens on agent redefinition of java.lang.System
            System.out.println("ATTACK FAILED: " + e.getMessage());
        }
    }

}
