import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import java.util.Date;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


public class Spider {
    private String url;

	public Spider(String _url)
	{
		url = _url;
	}

    public Vector<String> extractWords() throws ParserException
    {
        StringBean sb;
        sb = new StringBean ();
        sb.setURL (url);
        String content = sb.getStrings ();
        StringTokenizer st = new StringTokenizer(content);
        Vector<String> v = new Vector<String>();
        while(st.hasMoreTokens()){
            v.add(st.nextToken());
        }
        return v;
    }


    public Vector<String> extractLinks() throws ParserException
    {
        Vector<String> v_link = new Vector<String>();
        LinkBean lb = new LinkBean();
        lb.setURL(url);
        URL[] URL_array = lb.getLinks();
        for(int i=0; i<URL_array.length; i++){
            v_link.add(URL_array[i].toString());
        }
        return v_link;
    }


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

    
    public String getLastModifiedDate() {
        try {
            URL pageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
            long lastModifiedTimestamp = connection.getLastModified();
            if (lastModifiedTimestamp != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return sdf.format(new Date(lastModifiedTimestamp));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // need to implement the code to get the last modified date if it is not provided in the header
        return null;
    }


    public long getPageSize() {
        try {
            URL pageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
            long pageSize = connection.getContentLength();
            if (pageSize != -1){
                return pageSize;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // need to implement the code to get the page size if it is not provided in the header
        return -1;
    }
}
