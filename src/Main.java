import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Inserisci il seed URL: ");
        String seedURL = scanner.nextLine();

        if (!seedURL.startsWith("http://") && !seedURL.startsWith("https://")) {
            seedURL = "http://" + seedURL;
        }

        WebCrawler crawler = new WebCrawler();
        crawler.startCrawling(seedURL);

        scanner.close();
    }
}
