# SecurityFixer

Uses Byte Buddy to override the behavior of `System.setSecurityManager`.

NOTE: This is a demo, not a real security implementation.  For example, `System.getSecurityManager` isn't intercepted!  

## Compiling

```
mvn clean compile package
```

## Running

A little awkward.  Thanks to https://stackoverflow.com/questions/40795399/exception-on-invocation-of-java-agent-instrumented-via-bytebuddy I got Maven working.
  
Note that the intercepter has to be on the bootstrap classpath, so it must be packaged seperately from the agent:

```bash
java -javaagent:agent/target/securityfixer-agent-1.0-SNAPSHOT.jar=bootstrap/target/securityfixer-bootstrap-1.0-SNAPSHOT.jar \
    -jar example/target/securityfixer-example-1.0-SNAPSHOT.jar
```

You can use the attached `run.sh` script.

## Problems

For some reason, the static method override stops working in later versions of ByteBuddy, i.e. 1.4.1 or later.  I have not tracked down why exactly.