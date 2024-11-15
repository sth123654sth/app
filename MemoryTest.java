import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
// set JAVA_HOME=C:\Java\zulu11.70.15-ca-jdk11.0.22-win_x64
// set PATH=C:\Java\zulu11.70.15-ca-jdk11.0.22-win_x64\bin
//javac -encoding utf8 -classpath .;* MemoryTest.java
//java -classpath .;* MemoryTest
/**
 * 每个方法的参数m都是表示对应区间分配多少M内存
 * 
 * @author afei
 * @date 2018-09-28
 * @since 1.0.0
 */

public class MemoryTest {
    private static final int _1m = 1024 * 1024;
    private static final long THREAD_SLEEP_MS = 10 * 1000;

    public static void main(String[] args) throws Exception {
        youngAllocate(1000);
        oldAllocate(1000);
        metaspaceAllocate(200000);
        directMemoryAllocate(400);
        // threadStackAllocate(400);
        Thread.sleep(60000);
    }

    /**
     * 
     * 
     * @param count 重复定义的MyCalc对象数量
     * 
     * 
     */
    private static void metaspaceAllocate(int count) throws Exception {
        System.out.println("metaspace object count: " + count);
        Method declaredMethod = ClassLoader.class.getDeclaredMethod("defineClass",
                new Class[] { String.class, byte[].class, int.class, int.class });
        declaredMethod.setAccessible(true);
        File classFile = new File("SimpleDeadLock.class");
        byte[] bcs = new byte[(int) classFile.length()];
        try (InputStream is = new FileInputStream(classFile);) {
            // 将文件流读进byte数组
            while (is.read(bcs) != -1) {
            }

        }

        int outputCount = count / 10;
        for (int i = 1; i <= count; i++) {
            try {

                // 重复定义MyCalc这个类
                declaredMethod.invoke(

                        MemoryTest.class.getClassLoader(),

                        new Object[] { "MyCalc", bcs, 0, bcs.length });

            } catch (Throwable e) {
                // 重复定义类会抛出LinkageError: attempted duplicate class definition for name: "MyCal
                // 
                //
                //
                //
                //
                // "
                // System.err.println(e.getCause().getLocalizedMessage());
            }

            if (i >= outputCount && i % outputCount == 0) {
                System.out.println("i = " + i);
            }
        }
        System.out.println("metaspace end");
    }

    /**
     * 
     * 
     * @param m 分配多少M direct memory
     * 
     * 
     */

    private static void directMemoryAllocate(int m) {
        System.out.println("direct memory: " + m + "m");
        for (int i = 0; i < m; i++) {
            ByteBuffer.allocateDirect(_1m);
        }
        System.out.println("direct memory end");
    }

    /**
     * 
     * 
     * @param m 给young区分配多少M的数据
     * 
     * 
     */
    private static void youngAllocate(int m) {
        System.out.println("young: " + m + "m");
        for (int i = 0; i < m; i++) {
            byte[] test = new byte[_1m];
        }
        System.out.println("young end");
    }

    /**
     * 
     * 
     * 需要配置参数: -XX:PretenureSizeThreshold=2M, 并且结合CMS
     * 
     * 
     * @param m 给old区分配多少M的数据
     * 
     * 
     */

    private static void oldAllocate(int m) {
        System.out.println("old:   " + m + "m");
        for (int i = 0; i < m / 5; i++) {
            byte[] test = new byte[5 * _1m];
        }
        System.out.println("old end");
    }

    // 需要配置参数: -Xss10240k, 这里的实验以失败告终

    private static void threadStackAllocate(int m) {
        int threadCount = m / 10;
        System.out.println("thread stack count:" + threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                System.out.println("thread name: " + Thread.currentThread().getName());
                try {
                    while (true) {
                        Thread.sleep(THREAD_SLEEP_MS);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        System.out.println("thread stack end:" + threadCount);
    }
}