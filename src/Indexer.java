import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;

public class Indexer {
    private RecordManager recman;
    private HTree hashtable;

    public Indexer(String recordmanager, String objectname) throws IOException {
        recman = RecordManagerFactory.createRecordManager(recordmanager);
        long recid = recman.getNamedObject(objectname);

        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject("ht1", hashtable.getRecid());
        }
    }

    public void finalize() throws IOException {
        recman.commit();
        recman.close();
    }

    public void addEntry(String key, String data) throws IOException {
        if (hashtable.get(key) != null) {
            String temp_value = hashtable.get(word) + " doc" + String.valueOf(x) + " " + String.valueOf(y);
            hashtable.put(word, temp_value);
        } else {
            hashtable.put(word, "doc" + String.valueOf(x) + " " + String.valueOf(y));
        }
    }

}
