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

// class PageInfo 
// {
//     public String PageID;
//     public String PageTitle;
//     public String URL;
//     public String LastModificationDate;
//     public Long SizeofPage;

//     public PageInfo(String PageID, String PageTitle, String URL, String LastModificationDate, Long SizeofPage) {
//         this.PageID = PageID;
//         this.PageTitle = PageTitle;
//         this.URL = URL;
//         this.LastModificationDate = LastModificationDate;
//         this.SizeofPage = SizeofPage;
//     }
// }


public class Indexer {
    private RecordManager recman;
    private HTree hashtable;

    public Indexer(String recordmanager, String objectname) throws IOException {

        String folderPath = "../data";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); 
        }
        String dbPath = folderPath + File.separator + recordmanager;

        recman = RecordManagerFactory.createRecordManager(dbPath);
        long recid = recman.getNamedObject(objectname);
        // System.out.println(recid);
        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject(objectname, hashtable.getRecid());
        }
    }


    public void finalize() throws IOException {
        recman.commit();
        recman.close();
    }


    public void Mapper(String key, String value) throws IOException {
        hashtable.put(key, value);
    }
    

    public void addMapping(String key, String value) throws IOException {
        addMapping(key, value, false);
    }
    
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


    public void addPageInfo(String PageID, String PageTitle, String URL, String LastModificationDate, Long  SizeofPage) throws IOException {
        // if (hashtable.get(PageID) == null) {
        hashtable.put(PageID, PageTitle + "|" + URL + "|" + LastModificationDate + "|" + SizeofPage);
        // }
    }


    public void addPageChild(String PageID, Vector<String> ChildURL) throws IOException {
        if (ChildURL.size() == 0) {
            return;
        }
        String child = String.join(",", ChildURL);
        hashtable.put(PageID, child);
    }


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


    public void printAll() throws IOException {
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            String value = (String) hashtable.get(key);
            System.out.println(key + " " + value);
        }
    }


    public Map<Long, String[]> getAllValue(String separator) throws IOException {
        if (separator == null) {
            separator = ",";
        }
        else if (separator == "|") {
            separator = "\\|";
        }
        Map<Long, String[]> result = new HashMap<Long, String[]>();
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            String value= (String) hashtable.get(key);
            // System.out.println(value);
            String[] values = value.split(separator);
            result.put(Long.parseLong(key), values);
        }
        return result;
    }
    

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


    public long getSize() throws IOException {
        long size = 0;
        FastIterator iter = hashtable.keys();
        String key;
        while ((key = (String) iter.next()) != null) {
            size++;
        }
        return size;
    }


    public String getValue(String key) throws IOException {
        return (String)hashtable.get(key);
    }


    public Boolean containsKey(String key) throws IOException {
        return ((String)hashtable.get(key)==null) ? false : true;
    }

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
