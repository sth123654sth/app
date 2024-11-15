import java.util.*;

//import com.jvm.User;

// OOMTest 
// set JAVA_HOME=C:\Java\zulu11.70.15-ca-jdk11.0.22-win_x64
// set PATH=C:\Java\zulu11.70.15-ca-jdk11.0.22-win_x64\bin
// javac -encoding utf8 -classpath .;* OOMTest.java User.java
// java -classpath .;*  -Xms10m -Xmx10m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=\logs\heapdump.hprof -XX:+UseG1GC -XX:NewRatio=2 -XX:SurvivorRatio=4 -XX:+PrintGCDetails -Xlog:gc*:file=\logs\gc.log  OOMTest
public class OOMTest  {
    public static List<Object> list = new ArrayList<>();
    public static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (true) {
        list.add(new User(i++, UUID.randomUUID().toString()));
        new User(j--, UUID.randomUUID().toString());
         }
        }
}