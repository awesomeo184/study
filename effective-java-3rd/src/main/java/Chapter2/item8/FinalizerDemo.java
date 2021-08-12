package Chapter2.item8;

import java.io.FileInputStream;

public class FinalizerDemo {

    @Override
    protected void finalize() throws Throwable {
        System.out.println("Clean up");
    }

    public void hello() {
        System.out.println("hello");
    }
}
