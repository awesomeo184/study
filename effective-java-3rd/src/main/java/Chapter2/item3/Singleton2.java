package Chapter2.item3;

public class Singleton2 {

    private static final transient Singleton2 INSTANCE = new Singleton2();

    private Singleton2() {};

    public static Singleton2 getInstance() {
        return INSTANCE;
    }
}
