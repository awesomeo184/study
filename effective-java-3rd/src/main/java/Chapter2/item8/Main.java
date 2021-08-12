package Chapter2.item8;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        new Main().run();
    }

    private void run() {
        for (int i = 0; i < 1000000; i++) {
            FinalizerDemo finalizerDemo = new FinalizerDemo();
            finalizerDemo.hello();
        }
    }
}
