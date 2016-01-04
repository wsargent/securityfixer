package com.tersesystems.securityfixer.agent;

import com.tersesystems.securityfixer.MySystemInterceptor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class SecurityFixerAgent {

    public static void premain(String arg, Instrumentation inst) {
        install(arg, inst);
    }

    public static void agentmain(String arg, Instrumentation inst) {
        install(arg, inst);
    }

    static void install(String arg, Instrumentation inst)  {
        try {
            File file = new File(arg).getCanonicalFile();
            if (! file.exists()) {
                throw new IllegalStateException(arg + " does not exist");

            }

            if (! file.isFile()) {
                throw new IllegalStateException(arg + " is not a file");

            }
            if (! file.canRead()) {
                throw new IllegalStateException(arg + "cannot be read");

            }

            JarFile jarFile = new JarFile(file);
            inst.appendToBootstrapClassLoaderSearch(jarFile);

            AgentBuilder agentBuilder = createAgentBuilder(inst);
            agentBuilder.installOn(inst);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static AgentBuilder createAgentBuilder(Instrumentation inst) {
        final File folder = new File("bootstrap");
        folder.mkdir();

        final ElementMatcher.Junction<NamedElement> systemType = ElementMatchers.named("java.lang.System");
        AgentBuilder.Transformer transformer =
                (b, typeDescription) -> b.method(ElementMatchers.named("setSecurityManager"))
                .intercept(MethodDelegation.to(MySystemInterceptor.class));

        ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);
        final AgentBuilder agentBuilder = new AgentBuilder.Default()
                .enableBootstrapInjection(folder, inst)
                .withByteBuddy(byteBuddy)
                .withInitializationStrategy(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .withRedefinitionStrategy(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .withTypeStrategy(AgentBuilder.TypeStrategy.Default.REDEFINE)
                //.withListener(new AgentBuilder.Listener.StreamWriting(System.out))
                .type(systemType)
                .transform(transformer);

        return agentBuilder;
    }

}
