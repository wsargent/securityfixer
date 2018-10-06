# SecurityFixer

Uses Byte Buddy to override the behavior of `System.setSecurityManager`.

NOTE: This is a demo, not a real security implementation.  For example, `System.getSecurityManager` isn't intercepted!  

## Compiling

```
mvn clean compile package
```

## Running

A little awkward. You can use the attached `run.sh` script.

```bash
java -javaagent:agent/target/securityfixer-agent-2.0-SNAPSHOT.jar \
    -jar example/target/securityfixer-example-2.0-SNAPSHOT.jar

```

## Problems

I have this working with Byte Buddy 1.9.0 after some futzing with `@Advice` using the [generated bootstrap approach](https://stackoverflow.com/questions/44747219/byte-buddy-advice-onmethodexit-constructor-retransformation), but the static security manager must be public.

For the purposes of the demo I'm not worried about it.