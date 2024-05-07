import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class Indexer {
    private RecordManager recman;

    public Indexer(String dbPath) throws IOException {
        // Create data folder if not exists
        // String folderPath = "../data";
        // File folder = new File(folderPath);
        // if (!folder.exists()) {
        //     folder.mkdirs();
        // }
        // Database file path
        // String dbPath = folderPath + File.separator + recordmanager;

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
    public void addPageInfo(HTree hashtable, String PageID, String PageTitle, String URL, String LastModificationDate, Long SizeofPage, Integer maxFreq)
            throws IOException {
        hashtable.put(PageID, PageTitle + "|" + URL + "|" + LastModificationDate + "|" + SizeofPage + "|" + maxFreq);
    }

    // Add PageChildMapping entry to index
    public void addPageChild(HTree PageChildTable, String PageID, List<String> ChildURL) throws IOException {
        if (ChildURL.size() == 0) {
            return;
        }
        String child = String.join(",", ChildURL);
        PageChildTable.put(PageID, child);
    }

    // Convert PageParentMapping key from URL to PageID
    public void convertPageParent(HTree URLTable, HTree PageParentTable) throws IOException {
        List<String> keysToRemove = new ArrayList<>();

        Set<String> keys = new HashSet<>();
        FastIterator iter = PageParentTable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            keys.add(key);
            // String value = (String) PageParentTable.get(key);
            // String parentValue = (String) URLTable.get(key);
            // if (parentValue != null && !parentValue.isEmpty()) {
            //     PageParentTable.put(parentValue, value);
            // }
            // keysToRemove.add(key);
        }

        for (String currentKey : keys) {
            String value = (String) PageParentTable.get(currentKey);
            String[] values = value.split(",");
            List<String> updatedValues = new ArrayList<>();
            for (String parentKey : values) {
                String parentValue = (String) URLTable.get(parentKey);
                if (parentValue != null && !parentValue.isEmpty()) {
                    updatedValues.add(parentValue);
                }
            }
            // Update the value in PageParentTable
            if (!updatedValues.isEmpty()) {
                value = String.join(",", updatedValues);
                PageParentTable.put(currentKey, value);
            } else {
                // Add the key to keysToRemove list for removal
                keysToRemove.add(currentKey);
            }
        }

        for (String keyToRemove : keysToRemove) {
            PageParentTable.remove(keyToRemove);
        }
    }

    // Convert PageChildMapping entry to index
    public void convertPageChild(HTree URLTable, HTree PageChildTable) throws IOException {
        List<String> keysToRemove = new ArrayList<>();

        // Create a copy of the keys to avoid ConcurrentModificationException
        Set<String> keys = new HashSet<>();
        FastIterator iter = PageChildTable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            keys.add(key);
        }

        
    
        // Iterate over the copied keys
        for (String currentKey : keys) {
            String value = (String) PageChildTable.get(currentKey);
            String[] values = value.split(",");
            List<String> updatedValues = new ArrayList<>();
            for (String childKey : values) {
                String childValue = (String) URLTable.get(childKey);
                if (childValue != null && !childValue.isEmpty()) {
                    updatedValues.add(childValue);
                }
            }
            // Update the value in PageChildTable
            if (!updatedValues.isEmpty()) {
                value = String.join(",", updatedValues);
                PageChildTable.put(currentKey, value);
            } else {
                // Add the key to keysToRemove list for removal
                keysToRemove.add(currentKey);
            }
        }
    
        // Remove keys that need to be removed
        for (String keyToRemove : keysToRemove) {
            PageChildTable.remove(keyToRemove);
        }
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

    // Add TFIDF/Max entry to index
    // Then, add the page magnitude to the PageInfo
    public void addInvertedTFIDF(HTree PageInfo, HTree InvertedWord) throws IOException {
        FastIterator iter = InvertedWord.keys();
        String key;
        FastIterator iter2 = PageInfo.keys();
        String key2;
        Map<Long, Integer> max = new HashMap<Long, Integer>();
        Map<Long, Double> pageMag = new HashMap<Long, Double>();
        while ((key2 = (String) iter2.next()) != null) {
            String value = (String) PageInfo.get(key2);
            String[] values = value.split("\\|");
            max.put(Long.parseLong(key2), Integer.parseInt(values[4]));
            pageMag.put(Long.parseLong(key2), 0.0);
        }
        double n = getSize(PageInfo);
        while ((key = (String) iter.next()) != null) {
            String value = (String) InvertedWord.get(key);
            Map<Long, String[]> values = getInvertedWord(InvertedWord, key);
            for (Map.Entry<Long, String[]> entry : values.entrySet()) {
                String[] temp = entry.getValue();
                int maxFreq = max.get(entry.getKey());
                double TF = Double.parseDouble(temp[0]) / maxFreq;
                double IDF = Math.log(n / values.size());
                double TFIDF = Math.round(TF * IDF * 100000.0) / 100000.0;
                addInvertedWord(InvertedWord, key, String.valueOf(entry.getKey()), temp[0], String.valueOf(TFIDF));
                pageMag.put(entry.getKey(), pageMag.get(entry.getKey()) + Math.pow(TFIDF, 2));
            }
        }
        for (Map.Entry<Long, Double> entry : pageMag.entrySet()) {
            double mag = Math.sqrt(entry.getValue());
            PageInfo.put(String.valueOf(entry.getKey()), (String) PageInfo.get(String.valueOf(entry.getKey())) + "|" + mag);
        }
    }

    // Print all key-value pairs in the index
    public void printAll(HTree hashtable, Integer maxCount) throws IOException {
        Integer count = 0;
        FastIterator iter = hashtable.keys();
        String key;
        // Maximum number of key-value pairs to print is 30
        while (((key = (String) iter.next()) != null) && (count < maxCount)) {
            String value = (String) hashtable.get(key);
            System.out.println(key + " " + value);
            count++;
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
