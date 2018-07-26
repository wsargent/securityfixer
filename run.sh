
java -Xbootclasspath/p:agent/target/securityfixer-agent-1.0-SNAPSHOT.jar \
    -javaagent:agent/target/securityfixer-agent-1.0-SNAPSHOT.jar=bootstrap/target/securityfixer-bootstrap-1.0-SNAPSHOT.jar \
    securityfixer.Main
