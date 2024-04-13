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
import java.util.Comparator;
import java.util.stream.Collectors;

import jdbm.htree.HTree;


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
    public static HashMap<String, Integer> WordFreq(List<String> words, StopStem stopStem) {
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

    // Method to extract n-grams
    public static List<String> extractNGrams (List<String> words, StopStem stopStem, int n) {
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i < words.size() - n + 1; i++) {
            String ngram = "";
            for (int j = 0; j < n; j++) {
                String word = words.get(i + j).toLowerCase();
                if (stopStem.isStopWord(word)) {
                    ngram = "";
                    break;
                }
                ngram += stopStem.stem(word) + " ";
            }
            if (ngram != "")
                ngrams.add(ngram);
        }
        return ngrams;
    }

    // Method to crawl and index web pages
    public static void crawlAndIndexPages() {
        queue.add(staring_url);
        try {
            Indexer indexer = new Indexer ("database");

            HTree PageInfoIndexer = indexer.getOrCreateHTree("PageInfo");
            HTree PageURlMapping = indexer.getOrCreateHTree("PageURlMapping");
            HTree PageChild = indexer.getOrCreateHTree("PageChild");
            HTree WordMapping = indexer.getOrCreateHTree("WordMapping");
            HTree BodyWordMapping = indexer.getOrCreateHTree("BodyWordMapping");
            HTree InvertedBodyWord = indexer.getOrCreateHTree("InvertedBodyWord");


            StopStem stopStem = new StopStem(STOPWORDS);

            long PageID = indexer.getSize(PageInfoIndexer);

            // Loop until the queue is empty or maximum pages limit is reached
            while (!queue.isEmpty() && indexedPages.size() < max_pages) {
                String url = queue.poll();
                if (!visited.contains(url)) {
                    PageID++;
                    visited.add(url);
                    indexedPages.add(url);
                    try {
                        // If URL is already indexed, retrieve PageID from mapping
                        if (indexer.containsKey(PageURlMapping, url)) {
                            PageID = Long.valueOf(indexer.getValue(PageURlMapping, url));
                        } else {
                            indexer.addMapping(PageURlMapping, url, String.valueOf(PageID));
                        }
                        Spider spider = new Spider(url);

                        String lastModificationDate = spider.getLastModifiedDate();
                        
                        // check if the modification date is the newer than the one in the database

                        String dbLastModificationDate = null;
                        if (indexer.containsKey(PageInfoIndexer, String.valueOf(PageID)))
                            dbLastModificationDate = indexer.getValues(PageInfoIndexer, String.valueOf(PageID))[2];
                        
                        // if (dbLastModificationDate != null && dbLastModificationDate.compareTo(lastModificationDate) >= 0) {
                        //     System.out.println("Page already indexed, skipping: " + url);
                        //     continue;
                        // }

                        String title = spider.extractTitle();

                        Long pageSize = spider.getPageSize();
                        
                        List<String> words = spider.extractWords();
                        // Calculate word frequency
                        HashMap<String, Integer> wordFreq = WordFreq(words, stopStem);

                        List<String> two_gram = extractNGrams(words, stopStem, 2);
                        List<String> three_gram = extractNGrams(words, stopStem, 3);

                        // System.out.println("URL: " + url);
                        // System.out.println("title: " + title);
                        // for (String word : words) {
                        //     System.out.print(word + " ");
                        // }

                        // System.out.println("last modification date: " + lastModificationDate);

                        // System.out.println("page size: " + pageSize);
                        // System.out.println("words: " + wordFreq.size());

                        // for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
                        //     System.out.println(entry.getKey() + " " + entry.getValue());
                        // }

                        // System.out.println("two-grams: " + two_gram.size());
                        // for (String ngram : two_gram) {
                        //     System.out.println(ngram);
                        // }

                        // System.out.println("three-grams: " + three_gram.size());
                        // for (String ngram : three_gram) {
                        //     System.out.println(ngram);
                        // }

                        // Process each word and update indexers
                        for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
                            String word = entry.getKey();
                            Long wordid;
                            if (indexer.containsKey(WordMapping, word)) {
                                wordid = Long.valueOf(indexer.getValue(WordMapping, word));
                            } else {
                                wordid = indexer.getSize(WordMapping) + 1;
                                indexer.addMapping(WordMapping, word, String.valueOf(wordid));
                            };

                            Map<Long, String[]> invertedBody = indexer.getInvertedWord(InvertedBodyWord, String.valueOf(wordid)); 

                            indexer.addInvertedWord(InvertedBodyWord, String.valueOf(wordid), String.valueOf(PageID), String.valueOf(entry.getValue()), "1");

                            indexer.addMapping(BodyWordMapping, String.valueOf(PageID), String.valueOf(wordid) + "|" + String.valueOf(entry.getValue()), true);
                        }

                        // Extract child links and update PageChild indexer
                        List<String> links = spider.extractLinks();
                        List<String> child = new ArrayList<>();

                        indexer.addPageInfo(PageInfoIndexer, String.valueOf(PageID), title, url, lastModificationDate, pageSize);
                        for (String link : links) {
                            if (!visited.contains(link)) {
                                child.add(link);
                            }
                            if (!visited.contains(link) && !queue.contains(link)) {
                                queue.add(link);
                            }
                        }
                        indexer.addPageChild(PageURlMapping, PageChild, String.valueOf(PageID), child);

                    } catch (Exception e) {
                        // Print stack trace and continue crawling
                        e.printStackTrace();
                    }

                }
            }
            
            // Close indexers
            indexer.finalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to output indexed pages and related information to a file
    public static void outputFile() {
        String outputFilePath = "../docs/spider_result.txt"; 
        try {
            Indexer indexer = new Indexer ("database");

            HTree PageInfoIndexer = indexer.getOrCreateHTree("PageInfo");
            HTree PageURlMapping = indexer.getOrCreateHTree("PageURlMapping");
            HTree PageChild = indexer.getOrCreateHTree("PageChild");
            HTree WordMapping = indexer.getOrCreateHTree("WordMapping");
            HTree BodyWordMapping = indexer.getOrCreateHTree("BodyWordMapping");
            HTree InvertedBodyWord = indexer.getOrCreateHTree("InvertedBodyWord");

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

            // Retrieve page information
            Map<Long, String[]> pageInfo = indexer.getAllValue(PageInfoIndexer, "|");

            // Iterate over each page and write information to file
            for (Map.Entry<Long, String[]> entry : pageInfo.entrySet()) {
                Long PageID = entry.getKey();
                writer.write(entry.getValue()[0] + "\n"); // Title
                writer.write(entry.getValue()[1] + "\n"); // URL
                writer.write(entry.getValue()[2] + ", " + entry.getValue()[3] + "\n"); // Last modification date and page size
                
                // Retrieve word frequency for the page and write top 5 frequent words
                Map<Long, Long> freq  = indexer.getWordFreq(BodyWordMapping, String.valueOf(PageID));
                List<Map.Entry<Long, Long>> sortedEntries = freq.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());
                for (int i = 0; i < 5 && i < sortedEntries.size(); i++) {
                    Long wordID = sortedEntries.get(i).getKey();
                    String word = indexer.findKey(WordMapping, String.valueOf(wordID));
                    writer.write(word + " " + sortedEntries.get(i).getValue() + ", ");
                }
                writer.write("\n");

                // Write child links
                String childs = indexer.getValue(PageChild, String.valueOf(PageID));
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
            indexer.finalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void displayDB() {
        try {
            Indexer indexer = new Indexer ("database");

            HTree PageInfoIndexer = indexer.getOrCreateHTree("PageInfo");
            HTree PageURlMapping = indexer.getOrCreateHTree("PageURlMapping");
            HTree PageChild = indexer.getOrCreateHTree("PageChild");
            HTree WordMapping = indexer.getOrCreateHTree("WordMapping");
            HTree BodyWordMapping = indexer.getOrCreateHTree("BodyWordMapping");
            HTree InvertedBodyWord = indexer.getOrCreateHTree("InvertedBodyWord");

            // Print all key-value pairs in the index
            System.out.println("PageInfoIndexer");
            indexer.printAll(PageInfoIndexer);
            // System.out.println("PageURlMapping");
            // indexer.printAll(PageURlMapping);
            System.out.println("PageChild");
            indexer.printAll(PageChild);
            // System.out.println("WordMapping");
            // indexer.printAll(WordMapping);
            // System.out.println("BodyWordMapping");
            // indexer.printAll(BodyWordMapping);
            // System.out.println("InvertedBodyWord");
            // indexer.printAll(InvertedBodyWord);

            // Close indexers
            indexer.finalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
