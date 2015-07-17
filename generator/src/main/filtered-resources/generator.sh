#/bin/sh
JAVA_OPTS="-Xms1g -Xmx1g"
java $JAVA_OPTS -jar ${project.artifactId}-${project.version}.jar $*