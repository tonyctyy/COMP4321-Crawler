import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import org.htmlparser.util.ParserException;


public class TestProgram {
    private static final int MAX_PAGES = 30;
    private static Set<String> visited = new HashSet<>();
    private static Queue<String> queue = new LinkedList<>();
    private static List<String> indexedPages = new ArrayList<>();

    public static void crawlAndIndexPages(String startUrl) {
        queue.add(startUrl);

        while (!queue.isEmpty() && indexedPages.size() < MAX_PAGES) {
            String url = queue.poll();
            if (!visited.contains(url)) {
                visited.add(url);
                indexedPages.add(url);
                try{
                    Spider spider = new Spider(url);

                    Vector<String> words = spider.extractWords();
                    Vector<String> links = spider.extractLinks();
                    
                    for (String link : links) {
                        if (!visited.contains(link) && !queue.contains(link)) {
                            queue.add(link);
                        }
                    }
                }
                catch(ParserException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void displayIndexedPages() {
        System.out.println("Indexed Pages:");
        for (String url : indexedPages) {
            System.out.println(url);
        }
        System.out.println(indexedPages.size());
    }

    public static void main(String[] args) {
        String startUrl = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        crawlAndIndexPages(startUrl);
        displayIndexedPages();
    }
}


