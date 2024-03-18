import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.Comparator;
import java.util.stream.Collectors;


public class CrawlandIndex {
    private static String staring_url;
    private static int max_pages;
    private static Set<String> visited = new HashSet<>();
    private static Queue<String> queue = new LinkedList<>();
    private static List<String> indexedPages = new ArrayList<>();
    private static String STOPWORDS = "../docs/stopwords.txt";


    public CrawlandIndex(String _staring_url, int _max_pages) {
        staring_url = _staring_url;
        max_pages = _max_pages;

    }

    // Method to calculate word frequency
    public static HashMap<String, Integer> WordFreq(Vector<String> words, StopStem stopStem) {
        HashMap<String, Integer> wordFreq = new HashMap<String, Integer>();
        for (String word : words) {
            word = word.toLowerCase();
            if (!stopStem.isStopWord(word)) {
                String stemmedWord = stopStem.stem(word);
                if (wordFreq.containsKey(stemmedWord)) {
                    wordFreq.put(stemmedWord, wordFreq.get(stemmedWord) + 1);
                } else {
                    wordFreq.put(stemmedWord, 1);
                }
            }
        }
        return wordFreq;
    };

    // Method to crawl and index web pages
    public static void crawlAndIndexPages() {
        queue.add(staring_url);
        try {
            Indexer PageInfoIndexer = new Indexer("PageInfo", "PageInfo");
            Indexer PageURlMapping = new Indexer("PageURlMapping", "PageURlMapping");
            Indexer PageChild = new Indexer("PageChild", "PageChild");
            Indexer WordMapping = new Indexer("WordMapping", "WordMapping");
            Indexer BodyWordMapping = new Indexer("BodyWordMapping", "BodyWordMapping");
            Indexer InvertedBodyWord = new Indexer("InvertedBodyWord", "InvertedBodyWord");

            StopStem stopStem = new StopStem(STOPWORDS);

            long PageID = PageInfoIndexer.getSize();

            // Loop until the queue is empty or maximum pages limit is reached
            while (!queue.isEmpty() && indexedPages.size() < max_pages) {
                String url = queue.poll();
                if (!visited.contains(url)) {
                    PageID++;
                    visited.add(url);
                    indexedPages.add(url);
                    try {
                        // If URL is already indexed, retrieve PageID from mapping
                        if (PageURlMapping.containsKey(url)) {
                            PageID = Long.valueOf(PageURlMapping.getValue(url));
                        } else {
                            PageURlMapping.addMapping(url, String.valueOf(PageID));
                        }
                        Spider spider = new Spider(url);

                        String title = spider.extractTitle();

                        String lastModificationDate = spider.getLastModifiedDate();

                        Long pageSize = spider.getPageSize();
                        
                        Vector<String> words = spider.extractWords();
                        // Calculate word frequency
                        HashMap<String, Integer> wordFreq = WordFreq(words, stopStem);

                        // Process each word and update indexers
                        for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
                            String word = entry.getKey();
                            Long wordid;
                            if (WordMapping.containsKey(word)) {
                                wordid = Long.valueOf(WordMapping.getValue(word));
                            } else {
                                wordid = WordMapping.getSize() + 1;
                                WordMapping.addMapping(word, String.valueOf(wordid));
                            }

                            if (InvertedBodyWord.containsKey(String.valueOf(wordid))) {
                                Map<Long, String[]> result = InvertedBodyWord.getInvertedBody(String.valueOf(wordid));
                                if (!result.containsKey(PageID)) {
                                    InvertedBodyWord.addInvertedBodyWord(String.valueOf(wordid), String.valueOf(PageID), String.valueOf(entry.getValue()), "1");
                                }
                            } else {
                                InvertedBodyWord.addInvertedBodyWord(String.valueOf(wordid), String.valueOf(PageID), String.valueOf(entry.getValue()), "1");
                            }
                            BodyWordMapping.addMapping(String.valueOf(PageID), String.valueOf(wordid) + "|" + String.valueOf(entry.getValue()), true);
                        }

                        // Extract child links and update PageChild indexer
                        Vector<String> links = spider.extractLinks();
                        Vector<String> child = new Vector<String>();

                        PageInfoIndexer.addPageInfo(String.valueOf(PageID), title, url, lastModificationDate, pageSize);
                        for (String link : links) {
                            if (!visited.contains(link)) {
                                child.add(link);
                            }
                            if (!visited.contains(link) && !queue.contains(link)) {
                                queue.add(link);
                            }
                        }
                        PageChild.addPageChild(String.valueOf(PageID), child);

                    } catch (Exception e) {
                        // Print stack trace and continue crawling
                        e.printStackTrace();
                    }

                }
            }
            
            // Close indexers
            PageInfoIndexer.finalize();
            PageURlMapping.finalize();
            PageChild.finalize();
            WordMapping.finalize();
            BodyWordMapping.finalize();
            InvertedBodyWord.finalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to output indexed pages and related information to a file
    public static void outputFile() {
        String outputFilePath = "../docs/output.txt"; 
        try {
            Indexer PageInfoIndexer = new Indexer("PageInfo", "PageInfo");
            Indexer PageURlMapping = new Indexer("PageURlMapping", "PageURlMapping");
            Indexer PageChild = new Indexer("PageChild", "PageChild");
            Indexer WordMapping = new Indexer("WordMapping", "WordMapping");
            Indexer BodyWordMapping = new Indexer("BodyWordMapping", "BodyWordMapping");
            Indexer InvertedBodyWord = new Indexer("InvertedBodyWord", "InvertedBodyWord");

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

            // Retrieve page information
            Map<Long, String[]> pageInfo = PageInfoIndexer.getAllValue("|");

            // Iterate over each page and write information to file
            for (Map.Entry<Long, String[]> entry : pageInfo.entrySet()) {
                Long PageID = entry.getKey();
                writer.write(entry.getValue()[0] + "\n"); // Title
                writer.write(entry.getValue()[1] + "\n"); // URL
                writer.write(entry.getValue()[2] + ", " + entry.getValue()[3] + "\n"); // Last modification date and page size
                
                // Retrieve word frequency for the page and write top 5 frequent words
                Map<Long, Long> freq  = BodyWordMapping.getWordFreq(String.valueOf(PageID));
                List<Map.Entry<Long, Long>> sortedEntries = freq.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());
                for (int i = 0; i < 5 && i < sortedEntries.size(); i++) {
                    Long wordID = sortedEntries.get(i).getKey();
                    String word = WordMapping.findKey(String.valueOf(wordID));
                    writer.write(word + " " + sortedEntries.get(i).getValue() + ", ");
                }
                writer.write("\n");

                // Write child links
                String childs = PageChild.getValue(String.valueOf(PageID));
                if (childs != null) {
                    int count = 1;
                    String [] childArray = childs.split(",");
                    for (String child : childArray) {
                        if (count > 10) {
                            break;
                        }
                        writer.write(child + "\n");
                        count++;
                    }
                }
                writer.write("--------------------------------------------------\n");

            }

            writer.close();

            // Close indexers
            PageInfoIndexer.finalize();
            PageURlMapping.finalize();
            PageChild.finalize();
            WordMapping.finalize();
            BodyWordMapping.finalize();
            InvertedBodyWord.finalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
}
