import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.File;
import java.io.IOException;

public class Indexer {
    private RecordManager recman;
    private HTree hashtable;

    public Indexer(String recordmanager, String objectname) throws IOException {
        // Create data folder if not exists
        String folderPath = "../data";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); 
        }
        // Database file path
        String dbPath = folderPath + File.separator + recordmanager;

        // Create or load record manager and HTree
        recman = RecordManagerFactory.createRecordManager(dbPath);
        long recid = recman.getNamedObject(objectname);
        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject(objectname, hashtable.getRecid());
        }
    }

    // Close the record manager when object is destroyed
    public void finalize() throws IOException {
        recman.commit();
        recman.close();
    }

    // Store a single key-value pair in the index
    public void Mapper(String key, String value) throws IOException {
        hashtable.put(key, value);
    }

    // Add key-value pair to index with support for appending to existing values
    public void addMapping(String key, String value) throws IOException {
        addMapping(key, value, false);
    }
    
    // Add key-value pair to index with support for appending to existing values
    public void addMapping(String key, String value, Boolean isList) throws IOException {
        if (hashtable.get(key) == null) {
            hashtable.put(key, value);
        }
        else {
            String temp = (String)hashtable.get(key);
            if (isList) {
                hashtable.put(key, temp + ',' + value);
            }
            else {
                hashtable.put(key, temp + value);
            }
        }
    }

    // Add PageInfo entry to index
    public void addPageInfo(String PageID, String PageTitle, String URL, String LastModificationDate, Long  SizeofPage) throws IOException {
        hashtable.put(PageID, PageTitle + "|" + URL + "|" + LastModificationDate + "|" + SizeofPage);
    }

    // Add PageChildMapping entry to index
    public void addPageChild(String PageID, Vector<String> ChildURL) throws IOException {
        if (ChildURL.size() == 0) {
            return;
        }
        String child = String.join(",", ChildURL);
        hashtable.put(PageID, child);
    }

    // Add InvertedBodyWord entry to index
    public void addInvertedBodyWord(String WordID, String PageID, String Frequency, String TFIDF) throws IOException {
        String value = PageID + "|" + Frequency + "|" + TFIDF;
        if (hashtable.get(WordID) == null) {
            hashtable.put(WordID, value);
        }
        else {
            String temp = (String)hashtable.get(WordID);
            hashtable.put(WordID, temp + ',' + value);
        }
    }

    // Print all key-value pairs in the index
    public void printAll() throws IOException {
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            String value = (String) hashtable.get(key);
            System.out.println(key + " " + value);
        }
    }

    // Retrieve all values from the index with specified separator
    public Map<Long, String[]> getAllValue(String separator) throws IOException {
        if (separator == null) {
            separator = ",";
        }
        else if (separator.equals("|")) {
            separator = "\\|";
        }
        Map<Long, String[]> result = new HashMap<Long, String[]>();
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            String value= (String) hashtable.get(key);
            String[] values = value.split(separator);
            result.put(Long.parseLong(key), values);
        }
        return result;
    }

    // Retrieve InvertedBodyWord values from the index
    public Map<Long, String[]> getInvertedBody(String WordID) throws IOException {
        Map<Long, String[]> result = new HashMap<Long, String[]>();
        String value = (String)hashtable.get(WordID);
        if (value == null) {
            return result;
        }
        String[] values = value.split(",");
        for (String v : values) {
            String[] temp = v.split("\\|");
            result.put(Long.parseLong(temp[0]), new String[]{String.valueOf(temp[1]), String.valueOf(temp[2])});
        }
        return result;
    }

    // Retrieve WordFreq values from the index
    public Map<Long, Long> getWordFreq(String PageID) throws IOException {
        Map<Long, Long> result = new HashMap<Long, Long>();
        String value = (String)hashtable.get(PageID);
        if (value == null) {
            return result;
        }
        String[] values = value.split(",");
        for (String v : values) {
            String[] temp = v.split("\\|");
            result.put(Long.parseLong(temp[0]), Long.parseLong(temp[1]));
        }
        return result;
    }

    // Get the number of key-value pairs in the index
    public long getSize() throws IOException {
        long size = 0;
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            size++;
        }
        return size;
    }

    // Retrieve value for a given key from the index
    public String getValue(String key) throws IOException {
        return (String)hashtable.get(key);
    }

    // Check if the index contains a given key
    public Boolean containsKey(String key) throws IOException {
        return ((String)hashtable.get(key)==null) ? false : true;
    }

    // Find key for a given value in the index
    public String findKey(String value) throws IOException {
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            if (hashtable.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }
}
