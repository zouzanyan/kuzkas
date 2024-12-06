import core.KuzkasBootStrap;

import java.lang.management.ManagementFactory;

public class Application {

    public static void main(String[] args) {
        KuzkasBootStrap kuzkasBootStrap = new KuzkasBootStrap();
        kuzkasBootStrap.KuzkasStart();
        System.out.println("zouzanyan-feature测试");
    }

}
