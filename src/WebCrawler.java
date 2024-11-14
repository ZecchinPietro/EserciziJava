import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WebCrawler {
    private static final int MAX_DEPTH = 3;
    private static final int MAX_THREADS = 10;
    private final Set<String> visitedUrls = new HashSet<>();
    private final Set<Thread> activeThreads = new HashSet<>();

    public void startCrawling(String seedURL) {
        crawl(seedURL, 0);

        synchronized (activeThreads) {
            while (!activeThreads.isEmpty()) {
                try {
                    activeThreads.wait();
                } catch (InterruptedException e) {
                    System.out.println("Errore durante l'attesa dei thread: " + e.getMessage());
                }
            }
        }
    }

    private void crawl(String url, int depth) {
        if (depth > MAX_DEPTH || visitedUrls.contains(url)) {
            return;
        }

        synchronized (visitedUrls) {
            if (visitedUrls.contains(url)) {
                return;
            }
            visitedUrls.add(url);
        }

        Thread thread = new Thread(() -> {
            try {
                System.out.println("Crawling URL: " + url);
                Document doc = Jsoup.connect(url).get();

                if (depth < MAX_DEPTH) {
                    extractLinks(doc, depth + 1);
                }
            } catch (IOException e) {
                System.out.println("Errore durante il crawling di: " + url + " - " + e.getMessage());
            } finally {
                synchronized (activeThreads) {
                    activeThreads.remove(Thread.currentThread());
                    activeThreads.notifyAll();
                }
            }
        });

        synchronized (activeThreads) {
            while (activeThreads.size() >= MAX_THREADS) {
                try {
                    activeThreads.wait();
                } catch (InterruptedException e) {
                    System.out.println("Errore durante l'attesa dei thread: " + e.getMessage());
                }
            }
            activeThreads.add(thread);
        }

        thread.start();
    }

    private void extractLinks(Document doc, int depth) {
        Elements links = doc.select("a[href]");

        for (Element link : links) {
            String linkHref = link.absUrl("href");
            if (!visitedUrls.contains(linkHref)) {
                crawl(linkHref, depth);
            }
        }
    }
}
