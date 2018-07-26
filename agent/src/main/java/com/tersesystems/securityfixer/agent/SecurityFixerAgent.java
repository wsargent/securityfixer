package com.tersesystems.securityfixer.agent;

import com.tersesystems.securityfixer.bootstrap.MySystemInterceptor;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * Sets up the Java Instrumentation API so we can redefine java.lang.System.  Either premain or agentmain is
 * called, depending on the instrumentation install path.
 */
public class SecurityFixerAgent {

    public static void premain(String arg, Instrumentation inst) {
        install(arg, inst);
    }

    public static void agentmain(String arg, Instrumentation inst) {
        install(arg, inst);
    }

    /**
     * Installs the agent builder to the instrumentation API.
     *
     * @param arg the path to the interceptor JAR file.
     * @param inst instrumentation instance.
     */
    static void install(String arg, Instrumentation inst) {
        appendInterceptorToBootstrap(arg, inst);
        AgentBuilder agentBuilder = createAgentBuilder(inst);
        agentBuilder.installOn(inst);
    }

    /**
     * Appends the JAR file at "arg" to the bootstrap classloader search.
     *
     * @param arg the path to the interceptor JAR file.
     * @param inst instrumentation instance.
     */
    private static void appendInterceptorToBootstrap(String arg, Instrumentation inst) {
        try {
            if (arg == null) {
                String msg = "You must specify path to the interceptor JAR as a javaagent argument!";
                throw new IllegalStateException(msg);
            }

            File file = new File(arg).getCanonicalFile();
            if (!file.exists()) {
                throw new IllegalStateException(arg + " does not exist");
            }

            if (!file.isFile()) {
                throw new IllegalStateException(arg + " is not a file");

            }
            if (!file.canRead()) {
                throw new IllegalStateException(arg + " cannot be read");

            }
            JarFile jarFile = new JarFile(file);
            inst.appendToBootstrapClassLoaderSearch(jarFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates the AgentBuilder that will redefine the System class.
     * @param inst instrumentation instance.
     * @return an agent builder.
     */
    private static AgentBuilder createAgentBuilder(Instrumentation inst) {

        // Find me a class called "java.lang.System"
        final ElementMatcher.Junction<NamedElement> systemType = ElementMatchers.named("java.lang.System");

        // And then find a method called setSecurityManager and tell MySystemInterceptor to
        // intercept it (the method binding is smart enough to take it from there)
        final AgentBuilder.Transformer transformer =
                new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) {
                        return builder.method(ElementMatchers.named("setSecurityManager"))
                                .intercept(MethodDelegation.to(MySystemInterceptor.class));
                    }
                };

        // Disable a bunch of stuff and turn on redefine as the only option
        final ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);
        final AgentBuilder agentBuilder = new AgentBuilder.Default()
                .with(byteBuddy)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .type(systemType)
                .transform(transformer);

        return agentBuilder;
    }

}
