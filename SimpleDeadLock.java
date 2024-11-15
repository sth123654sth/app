import java.util.*;
//import sun.jvm.hotspot.tools.Tool;
// java 线程死锁，程序 hang 住
// set JAVA_HOME=C:\Java\zulu11.70.15-ca-jdk11.0.22-win_x64
// set PATH=C:\Java\zulu11.70.15-ca-jdk11.0.22-win_x64\bin
// javac -encoding utf8 -classpath .;* SimpleDeadLock.java
// java -classpath .;*  -Xms640m -Xmx640m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=\logs\heapdump.hprof -XX:+UseG1GC -XX:NewRatio=2 -XX:SurvivorRatio=4 -XX:+PrintGCDetails -Xlog:gc*:file=\logs\gc.log  SimpleDeadLock
public class SimpleDeadLock extends Thread {
    public static Object l1 = new Object();
    public static Object l2 = new Object();
    private int index;

    public static void main(String[] a) {
        System.out.println("Object 1 is: " + l1);
        System.out.println("Object 2 is: " + l2);

        Thread t1 = new Thread1();
        Thread t2 = new Thread2();
        t1.start();
        t2.start();
        System.out.println("Thread1's ID is: " + t1.getId());
        System.out.println("Thread2's ID is: " + t2.getId());
    }

    private static class Thread1 extends Thread {
        public void run() {
            synchronized (l1) {
                System.out.println("Thread 1: Holding lock 1...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                System.out.println("Thread 1: Waiting for lock 2...");
                synchronized (l2) {
                    System.out.println("Thread 1: Holding lock 1 & 2...");
                }
            }
        }
    }

    private static class Thread2 extends Thread {
        public void run() {
            synchronized (l2) {
                System.out.println("Thread 2: Holding lock 2...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                System.out.println("Thread 2: Waiting for lock 1...");
                synchronized (l1) {
                    System.out.println("Thread 2: Holding lock 2 & 1...");
                }
            }
        }
    }
}