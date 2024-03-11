import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);

        for (int i = 0; i < 10; i++) {
            crptApi.createDocument("this is document", "this is my SIGNATURE");
        }
    }
}