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


import org.htmlparser.util.ParserException;


public class TestProgram {
    private static final int MAX_PAGES = 30;
    private static Set<String> visited = new HashSet<>();
    private static Queue<String> queue = new LinkedList<>();
    private static List<String> indexedPages = new ArrayList<>();
    private static String STOPWORDS = "../docs/stopwords.txt";


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


    public static void crawlAndIndexPages(String startUrl) {
        queue.add(startUrl);
        try {
            Indexer PageInfoIndexer = new Indexer("PageInfo", "PageInfo");
            Indexer PageURlMapping = new Indexer("PageURlMapping", "PageURlMapping");
            Indexer PageChild = new Indexer("PageChild", "PageChild");
            Indexer WordMapping = new Indexer("WordMapping", "WordMapping");
            Indexer BodyWordMapping = new Indexer("BodyWordMapping", "BodyWordMapping");
            Indexer InvertedBodyWord = new Indexer("InvertedBodyWord", "InvertedBodyWord");

            StopStem stopStem = new StopStem(STOPWORDS);

            long PageID = PageInfoIndexer.getSize();

            while (!queue.isEmpty() && indexedPages.size() < MAX_PAGES) {
                String url = queue.poll();
                if (!visited.contains(url)) {
                    PageID++;
                    visited.add(url);
                    indexedPages.add(url);
                    // we assume all are just create new value now
                    try{
                        if (PageURlMapping.containsKey(url)) {
                            PageID = Long.valueOf(PageURlMapping.getValue(url));
                            // need to check the modification date later. Now, we skip it first.

                        } else {
                            PageURlMapping.addMapping(url, String.valueOf(PageID));
                        }
                        Spider spider = new Spider(url);

                        String title = spider.extractTitle();

                        String lastModificationDate = spider.getLastModifiedDate();

                        Long pageSize = spider.getPageSize();
                        
                        Vector<String> words = spider.extractWords();
                        // We iterate all the words twice here, we can optimize this later
                        HashMap<String, Integer> wordFreq = WordFreq(words, stopStem);

                        for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
                            String word = entry.getKey();
                            Long wordid;
                            if (WordMapping.containsKey(word)) {
                                wordid = Long.valueOf(WordMapping.getValue(word));
                            }
                            else {
                                wordid = WordMapping.getSize() + 1;
                                WordMapping.addMapping(word, String.valueOf(wordid));
                            }

                            if (InvertedBodyWord.containsKey(String.valueOf(wordid))) {
                                Map<Long, String[]> result = InvertedBodyWord.getInvertedBody(String.valueOf(wordid));
                                if (!result.containsKey(PageID)) {
                                    // String[] temp = result.get(PageID);
                                    // System.out.println(PageID);
                                    // System.out.println(temp);
                                    // Integer frequency = Integer.valueOf(temp[0]) + entry.getValue();
                                    // InvertedBodyWord.addInvertedBodyWord(String.valueOf(wordid), String.valueOf(PageID), String.valueOf(frequency), "1");
                                // }
                                // else {
                                    InvertedBodyWord.addInvertedBodyWord(String.valueOf(wordid), String.valueOf(PageID), String.valueOf(entry.getValue()), "1");
                                }
                            }
                            else {
                                InvertedBodyWord.addInvertedBodyWord(String.valueOf(wordid), String.valueOf(PageID), String.valueOf(entry.getValue()), "1");
                            }
                            BodyWordMapping.addMapping(String.valueOf(PageID), String.valueOf(wordid) + "|" + String.valueOf(entry.getValue()), true);
                        }

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

                    }
                    catch(ParserException e)
                    {
                        e.printStackTrace();
                    }

                }
            }
            
            // System.out.println("PageInfoIndexer:");
            // System.out.println(PageInfoIndexer.getSize());
            // PageInfoIndexer.printAll();
            // InvertedBodyWord.printAll();
            // System.out.println("PageChild:");
            // PageChild.printAll();

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


    public static void displayIndexedPages() {
        System.out.println("Indexed Pages:");
        for (String url : indexedPages) {
            System.out.println(url);
        }
        System.out.println(indexedPages.size());
    }


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

            Map<Long, String[]> pageInfo = PageInfoIndexer.getAllValue("|");

            for (Map.Entry<Long, String[]> entry : pageInfo.entrySet()) {
                Long PageID = entry.getKey();
                // System.out.println(Arrays.toString(entry.getValue()));
                // writer.write("PageID: " + entry.getKey() + "\n");
                writer.write(entry.getValue()[0] + "\n");
                writer.write(entry.getValue()[1] + "\n");
                writer.write(entry.getValue()[2] + ", " + entry.getValue()[3] + "\n");
                
                Map<Long, Long> freq  = BodyWordMapping.getWordFreq(String.valueOf(PageID));
                // find the most frequent word
                List<Map.Entry<Long, Long>> sortedEntries = freq.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());

                for (int i = 0; i < 5 && i < sortedEntries.size(); i++) {
                    Long wordID = sortedEntries.get(i).getKey();
                    String word = WordMapping.findKey(String.valueOf(wordID));
                    writer.write(word + " " + sortedEntries.get(i).getValue() + ", ");
                }
                writer.write("\n");
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


    public static void main(String[] args) {
        String startUrl = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        crawlAndIndexPages(startUrl);
        outputFile();
        // displayIndexedPages();
    }
}


