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
import java.net.URL;


public class Spider {
    private String url;

	public Spider(String _url)
	{
		url = _url;
	}

    public Vector<String> extractWords() throws ParserException
    {
        // extract words in url and return them
        // use StringTokenizer to tokenize the result from StringBean
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
        // extract links in url and return them
        Vector<String> v_link = new Vector<String>();
        LinkBean lb = new LinkBean();
        lb.setURL(url);
        URL[] URL_array = lb.getLinks();
        for(int i=0; i<URL_array.length; i++){
            v_link.add(URL_array[i].toString());
        }
        return v_link;
    }
}
