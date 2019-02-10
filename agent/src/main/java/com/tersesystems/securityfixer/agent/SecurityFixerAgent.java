package com.tersesystems.securityfixer.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.TypeStrategy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.StringMatcher;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;

import static java.util.Collections.singletonMap;
import static net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import static net.bytebuddy.dynamic.ClassFileLocator.ForClassLoader.read;
import static net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation.Target.BOOTSTRAP;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class SecurityFixerAgent {
    private static final Class<?> INTERCEPTOR_CLASS = MySystemInterceptor.class;

    private SecurityFixerAgent() {
    }

    // From https://stackoverflow.com/questions/44747219/byte-buddy-advice-onmethodexit-constructor-retransformation
    public static void premain(String arg, Instrumentation instrumentation) throws Exception {
        injectBootstrapClasses(instrumentation);
        new AgentBuilder.Default()
                .with(RedefinitionStrategy.RETRANSFORMATION)
                .with(InitializationStrategy.NoOp.INSTANCE)
                .with(TypeStrategy.Default.REDEFINE)
                .ignore(new AgentBuilder.RawMatcher.ForElementMatchers(nameStartsWith("net.bytebuddy.").or(isSynthetic()), any(), any()))
                .with(new Listener.Filtering(
                        new StringMatcher("java.lang.System", StringMatcher.Mode.EQUALS_FULLY),
                        Listener.StreamWriting.toSystemOut()))
                .type(named("java.lang.System"))
                .transform((builder, type, classLoader, module) ->
                        builder.visit(Advice.to(INTERCEPTOR_CLASS).on(named("setSecurityManager")))
                )
                .installOn(instrumentation);
    }

    private static void injectBootstrapClasses(Instrumentation instrumentation) throws IOException {
        File temp = Files.createTempDirectory("tmp").toFile();
        temp.deleteOnExit();

        ClassInjector.UsingInstrumentation.of(temp, BOOTSTRAP, instrumentation)
            .inject(singletonMap(new ForLoadedType(INTERCEPTOR_CLASS), read(INTERCEPTOR_CLASS)));
    }
}