package util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpUtil {
    public static void main(String[] args) {
        Path upload_files = Paths.get("upload_files");
        Path test = upload_files.resolve("a.txt").normalize();
        System.out.println(test.toAbsolutePath());
    }
}

class ProcessIdExample {
    public static void main(String[] args) throws InterruptedException {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        System.out.println(ManagementFactory.getThreadMXBean().getThreadCount());

        System.out.println(runtimeMXBean.getVmName());

        String jvmName = runtimeMXBean.getName();
        int index = jvmName.indexOf('@');
        if (index != -1) {
            String pid = jvmName.substring(0, index);
            System.out.println("Current Process ID: " + pid);
        } else {
            System.out.println("Could not determine the process ID.");
        }
//        while (true){
//            System.out.println(memoryMXBean.getHeapMemoryUsage().getUsed());
//            System.out.println(memoryMXBean.getNonHeapMemoryUsage().getUsed());
//            Thread.sleep(1000);
//        }

    }
}
