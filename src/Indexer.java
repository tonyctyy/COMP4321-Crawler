import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class Indexer {
    private RecordManager recman;

    public Indexer(String recordmanager) throws IOException {
        // Create data folder if not exists
        String folderPath = "../data";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // Database file path
        String dbPath = folderPath + File.separator + recordmanager;

        // Create or load record manager
        recman = RecordManagerFactory.createRecordManager(dbPath);
    }
    
    //  Create or load HTree object
    public HTree getOrCreateHTree(String objectname) throws IOException {
        long recid = recman.getNamedObject(objectname);
        HTree hashtable;
        if (recid != 0) {
            hashtable = HTree.load(recman, recid);
        } else {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject(objectname, hashtable.getRecid());
        }
        return hashtable;
    }

    // Close the record manager when object is destroyed
    public void finalize() throws IOException {
        recman.commit();
        recman.close();
    }

    // Store a single key-value pair in the index
    public void Mapper(HTree hashtable, String key, String value) throws IOException {
        hashtable.put(key, value);
    }

    // Add key-value pair to index with support for appending to existing values
    public void addMapping(HTree hashtable, String key, String value) throws IOException {
        addMapping(hashtable, key, value, false);
    }

    // Add key-value pair to index with support for appending to existing values
    public void addMapping(HTree hashtable, String key, String value, Boolean isList) throws IOException {
        if (hashtable.get(key) == null) {
            hashtable.put(key, value);
        } else {
            String temp = (String) hashtable.get(key);
            if (isList) {
                String[] tempArray = temp.split(",");
                for (int i = 0; i < tempArray.length; i++) {
                    String[] tempArray2 = tempArray[i].split("\\|");
                    // update the value
                    if (tempArray2[0].equals(value.split("\\|")[0])) {
                        tempArray[i] = value;
                        temp = String.join(",", tempArray);
                        hashtable.put(key, temp);
                        return;
                    }
                    // add to the middle of the list
                    else if (Long.parseLong(tempArray2[0]) > Long.parseLong(value.split("\\|")[0])) {
                        String[] tempArray3 = Arrays.copyOfRange(tempArray, 0, i+1);
                        String[] tempArray4 = Arrays.copyOfRange(tempArray, i, tempArray.length);
                        tempArray3[i] = value;
                        tempArray = Arrays.copyOf(tempArray3, tempArray3.length + tempArray4.length);
                        System.arraycopy(tempArray4, 0, tempArray, tempArray3.length, tempArray4.length);
                        temp = String.join(",", tempArray);
                        hashtable.put(key, temp);
                        return;
                    }
                }
                // add to the end of the list
                temp = temp + "," + value;
                hashtable.put(key, temp);
            } else {
                hashtable.put(key, value);
            }
        }
    }

    // Add PageInfo entry to index
    public void addPageInfo(HTree hashtable, String PageID, String PageTitle, String URL, String LastModificationDate, Long SizeofPage)
            throws IOException {
        hashtable.put(PageID, PageTitle + "|" + URL + "|" + LastModificationDate + "|" + SizeofPage);
    }

    // Add PageChildMapping entry to index
    public void addPageChild(HTree URLTable, HTree PageChildTable, String PageID, List<String> ChildURL) throws IOException {
        if (ChildURL.size() == 0) {
            return;
        }
        // Convert ChildURL to PageID
        for (int i = 0; i < ChildURL.size(); i++) {
            String child = (String) URLTable.get(ChildURL.get(i));
            if (child == null) {
                // String ID = String.valueOf(getSize(URLTable) + 1);
                // addMapping(URLTable, ChildURL.get(i), ID);
                // child = ID;
                continue;
            }
            ChildURL.set(i, child);
        }
        String child = String.join(",", ChildURL);
        PageChildTable.put(PageID, child);
    }

    // Add InvertedBodyWord entry to index
    public void addInvertedWord(HTree hashtable, String WordID, String PageID, String Frequency, String TFIDF) throws IOException {
        String value = PageID + "|" + Frequency + "|" + TFIDF;
        if (hashtable.get(WordID) == null) {
            hashtable.put(WordID, value);
        } else {
            String temp = (String) hashtable.get(WordID);
            hashtable.put(WordID, temp + ',' + value);

            String[] tempArray = temp.split(",");
            for (int i = 0; i < tempArray.length; i++) {
                String[] tempArray2 = tempArray[i].split("\\|");
                // update the value
                if (tempArray2[0].equals(PageID)) {
                    tempArray[i] = value;
                    temp = String.join(",", tempArray);
                    hashtable.put(WordID, temp);
                    return;
                }
                // add to the middle of the list
                else if (Long.parseLong(tempArray2[0]) > Long.parseLong(PageID)) {
                    String[] tempArray3 = Arrays.copyOfRange(tempArray, 0, i+1);
                    String[] tempArray4 = Arrays.copyOfRange(tempArray, i, tempArray.length);
                    tempArray3[i] = value;
                    tempArray = Arrays.copyOf(tempArray3, tempArray3.length + tempArray4.length);
                    System.arraycopy(tempArray4, 0, tempArray, tempArray3.length, tempArray4.length);
                    temp = String.join(",", tempArray);
                    hashtable.put(WordID, temp);
                    return;
                }
            }
            // add to the end of the list
            temp = temp + "," + value;
            hashtable.put(WordID, temp);
        }
    }

    // Print all key-value pairs in the index
    public void printAll(HTree hashtable) throws IOException {
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            String value = (String) hashtable.get(key);
            System.out.println(key + " " + value);
        }
    }

    // Retrieve all values from the index with specified separator
    public Map<Long, String[]> getAllValue(HTree hashtable, String separator) throws IOException {
        if (separator == null) {
            separator = ",";
        } else if (separator.equals("|")) {
            separator = "\\|";
        }
        Map<Long, String[]> result = new HashMap<Long, String[]>();
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            String value = (String) hashtable.get(key);
            String[] values = value.split(separator);
            result.put(Long.parseLong(key), values);
        }
        return result;
    }

    // Retrieve InvertedBodyWord values from the index
    public Map<Long, String[]> getInvertedWord(HTree hashtable, String WordID) throws IOException {
        Map<Long, String[]> result = new HashMap<Long, String[]>();
        String value = (String) hashtable.get(WordID);
        if (value == null) {
            return result;
        }
        String[] values = value.split(",");
        for (String v : values) {
            String[] temp = v.split("\\|");
            // 0: PageID (Long), 1: Frequency (String[0]), 2: TFIDF (String[1])
            result.put(Long.parseLong(temp[0]), new String[] { String.valueOf(temp[1]), String.valueOf(temp[2]) });
        }
        return result;
    }

    // Retrieve WordFreq values from the index
    public Map<Long, Long> getWordFreq(HTree hashtable, String PageID) throws IOException {
        Map<Long, Long> result = new HashMap<Long, Long>();
        String value = (String) hashtable.get(PageID);
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
    public long getSize(HTree hashtable) throws IOException {
        long size = 0;
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            size++;
        }
        return size;
    }

    // Retrieve value for a given key from the index
    public String getValue(HTree hashtable, String key) throws IOException {
        return (String) hashtable.get(key);
    }

    // Retrieve values for a given key from the index
    public String[] getValues(HTree hashtable, String key) throws IOException {
        return ((String) hashtable.get(key)).split("\\|");
    }

    // Check if the index contains a given key
    public Boolean containsKey(HTree hashtable, String key) throws IOException {
        return ((String) hashtable.get(key) == null) ? false : true;
    }

    // Find key for a given value in the index
    public String findKey(HTree hashtable, String value) throws IOException {
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
