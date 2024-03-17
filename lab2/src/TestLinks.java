import org.htmlparser.beans.HTMLLinkBean;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

public class TestLinks
{
    public static void main(String[] args) 
    {
	try{
        URL url = new URL("https://www.opentext.com/solutions/industry/insurance");
        URLConnection uc = url.openConnection();
        
        HTMLLinkBean hlb = new HTMLLinkBean();
        System.out.println("The following is base-http connection: ");
        hlb.setConnection(uc);
        System.out.println(hlb.getURL());
        URL [] u = hlb.getLinks();
        System.out.println("The following are links in the page");
        HashSet<String> visitedLinks = new HashSet<>();
        for (int i = 0; i < u.length; i++) {
            String link = u[i].toString();
            if (!visitedLinks.contains(link)) {
                visitedLinks.add(link);
                System.out.println(link);
            } else {
                System.out.println("[Repeated] " + link);
            }
        }
	}
	catch(Exception ex)
	{
		System.err.println(ex.toString());
	}
    }
}