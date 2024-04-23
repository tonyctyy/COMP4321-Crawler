import java.util.ArrayList;
import java.util.List;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.htmlparser.tags.TitleTag;
import java.util.HashMap;

public class Spider {
    private String url;

    // Constructor to initialize Spider with a URL
    public Spider(String _url) {
        url = _url;
    }

    public HashMap<String, Integer> addFrequency(HashMap<String, Integer> wordFreq, String word) {
        if (wordFreq.containsKey(word)) {
            wordFreq.put(word, wordFreq.get(word) + 1);
        } else {
            wordFreq.put(word, 1);
        }
        return wordFreq;
    }

    // Extracts words from the webpage 1-gram, 2-gram, 3-gram (after stop stem)
    // Returns a hashmap of words and their frequencies
    public HashMap<String, Integer> extractWords(StopStem stopStem) throws ParserException {
        StringBean sb = new StringBean();
        sb.setURL(url);
        String content = sb.getStrings();

        // remove the title from the list of words
        String title = extractTitle();
        int titleIndex = content.indexOf(title);
        if (titleIndex != -1) {
            content = content.substring(titleIndex + title.length());
        }

        StringTokenizer st = new StringTokenizer(content);
        HashMap<String, Integer> wordFreq = new HashMap<String, Integer>();

        String prev = "";
        String prev_2 = "";
        while (st.hasMoreTokens()) {
            String this_word = st.nextToken().toLowerCase();
            String next_word = "";
            String next_2_word = "";
            String this_stem = stopStem.stem(this_word);
            String next_stem = "";
            String next_2_stem = "";
            boolean this_is_stop = stopStem.isStopWord(this_word);
            boolean next_is_stop = true;
            boolean next_2_is_stop = true;
            if (st.hasMoreTokens()){
                next_word = st.nextToken().toLowerCase();
                if (next_word!=""){
                    next_stem = stopStem.stem(next_word);
                    next_is_stop = stopStem.isStopWord(next_word);
                }
                if (st.hasMoreTokens()){
                    next_2_word = st.nextToken().toLowerCase();
                    if (next_2_word!=""){
                        next_2_stem = stopStem.stem(next_2_word);
                        next_2_is_stop = stopStem.isStopWord(next_2_word);
                    }
                }
            }

            if (!this_is_stop){
                wordFreq = addFrequency(wordFreq, this_stem);
                if (!next_is_stop){
                    wordFreq = addFrequency(wordFreq, this_stem + " " + next_stem);
                    if(!next_2_is_stop){
                        wordFreq = addFrequency(wordFreq, this_stem + " " + next_stem + " " + next_2_stem);
                    }
                }
                if (prev != ""){
                    wordFreq = addFrequency(wordFreq, prev + " " + this_stem);
                    if (prev_2 != ""){
                        wordFreq = addFrequency(wordFreq, prev_2 + " " + prev + " " + this_stem);
                    }
                }
            }

            if (!next_is_stop){
                wordFreq = addFrequency(wordFreq, next_stem);
                if ((!this_is_stop) && (prev != "")){
                    wordFreq = addFrequency(wordFreq, prev + " " + this_stem + " " + next_stem);
                }
                if (!next_2_is_stop){
                    wordFreq = addFrequency(wordFreq, next_stem + " " + next_2_stem);
                }
                prev_2 = next_stem;
            }
            else {
                prev_2 = "";
            }

            if (!next_2_is_stop){
                wordFreq = addFrequency(wordFreq, next_2_stem);
                prev = next_2_stem;
            }
            else {
                prev = "";
            }

        }
        return wordFreq;
    }

    // Extracts links from the webpage
    public List<String> extractLinks() throws ParserException {
        List<String> list = new ArrayList<>();
        LinkBean lb = new LinkBean();
        lb.setURL(url);
        URL[] URL_array = lb.getLinks();
        for (int i = 0; i < URL_array.length; i++) {
            list.add(URL_array[i].toString());
        }
        return list;
    }

    // Extracts the title of the webpage
    public String extractTitle() throws ParserException {
        try {
            Parser parser = new Parser(url);
            NodeClassFilter filter = new NodeClassFilter(TitleTag.class);
            NodeList list = parser.extractAllNodesThatMatch(filter);
            if (list.size() > 0) {
                TitleTag titleTag = (TitleTag) list.elementAt(0);
                return titleTag.getStringText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retrieves the last modified date of the webpage
    public String getLastModifiedDate() {
        try {
            URL pageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
            long lastModifiedTimestamp = connection.getLastModified();
            if (lastModifiedTimestamp != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return sdf.format(new Date(lastModifiedTimestamp));
            }

            // If the last modified date is not available in the HTTP header, try to extract it from the webpage

            Parser parser = new Parser(url);
            NodeClassFilter filter = new NodeClassFilter(MetaTag.class);
            NodeList metaTags = parser.extractAllNodesThatMatch(filter);
            for (int i = 0; i < metaTags.size(); i++) {
                MetaTag metaTag = (MetaTag) metaTags.elementAt(i);
                if (metaTag.getMetaTagName().equalsIgnoreCase("last-modified")) {
                    return metaTag.getAttribute("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Retrieves the size of the webpage
    public long getPageSize() {
        try {
            URL pageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
            long pageSize = connection.getContentLength();
            if (pageSize != -1) {
                return pageSize;
            }
            else {
                StringBean sb = new StringBean();
                sb.setURL(url);
                String content = sb.getStrings();
                return content.length();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
