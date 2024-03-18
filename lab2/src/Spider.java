import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;


public class Spider
{
	private String url;
	private int maxDepth;
	private int fetchSize;
	HashSet<String> visitedLinks = new HashSet<>();
	private int maxDisplay;

	Spider(String _url, int _maxDepth, int _fetchSize, int _maxDisplay)
	{
		url = _url;
		maxDepth = _maxDepth;
		fetchSize = _fetchSize;
		maxDisplay = _maxDisplay;
	}
	
	public Vector<String> extractWords(String _url) throws ParserException
	{
		// extract words in url and return them
		// use StringTokenizer to tokenize the result from StringBean
		// ADD YOUR CODES HERE
		StringBean sb;

        sb = new StringBean();
        sb.setURL(_url);

		StringTokenizer tokenizer = new StringTokenizer(sb.getStrings());
		Vector<String> v_word = new Vector<String>();
		while (tokenizer.hasMoreTokens()) {
			v_word.add(tokenizer.nextToken());

		}
		return v_word;
	}
	
	public Vector<String> extractLinks(String _url) throws ParserException
	{
		// extract links in url and return them
		// ADD YOUR CODES HERE
		Vector<String> v_link = new Vector<String>();
	    LinkBean lb = new LinkBean();
	    lb.setURL(_url);
	    URL[] URL_array = lb.getLinks();
	    for(int i=0; i<URL_array.length; i++){
			v_link.add(URL_array[i].toString());
	    }
		return v_link;
	}

	private void DFSfetchPage(String _url, int currentDepth) throws ParserException
	{
		printContent(_url);

		//System.out.println("=======================================================");
		//System.out.println("Current depth: " + currentDepth);
		//System.out.println("=======================================================");

		//printWord(extractWords(thisUrl), thisUrl);
		visitedLinks.add(_url);
		Vector<String> childLink = extractLinks(_url);
		printChildLinks(childLink);
		if (currentDepth > 0)
			for(int i = 0; i < childLink.size(); i++)		
			{
				String thisLink = childLink.get(i);
				if (!visitedLinks.contains(thisLink)) {
					if (visitedLinks.size() < fetchSize) {
						DFSfetchPage(childLink.get(i), currentDepth - 1);
					} else {
						break;
					}
				}
			}
	}

	private void BFSfetchPage(String _url, int maxDepth) throws ParserException 
	{
		Queue<String> queue = new LinkedList<>();
		int currentDepth = 0;

		queue.add(_url);
		visitedLinks.add(_url);

		while (!queue.isEmpty() && currentDepth <= maxDepth && visitedLinks.size() < fetchSize) {
			int size = queue.size();

			for (int i = 0; i < size; i++) {
				String currentUrl = queue.poll();

				//System.out.println("=======================================================");
				//System.out.println("Current depth: " + currentDepth);
				//System.out.println("=======================================================");
				printContent(currentUrl);

				Vector<String> words = extractWords(_url);
				printWordFrequency(sortWordFrequency(getProcessedWords(words)));

				Vector<String> childLinks = extractLinks(currentUrl);
				printChildLinks(childLinks);

				for (String childLink : childLinks) {
					if (!visitedLinks.contains(childLink)) {
						if (visitedLinks.size() < fetchSize) {
							visitedLinks.add(childLink);
							queue.add(childLink);
						} else {
							break;
						}
					}
				}
			}

        	currentDepth++;
    	}
	}

	public void printContent(String _url)
	{
		System.out.println("-------------------------------------------------------------------------------------------------------");
		try {
			// Create a new parser for the URL
			Parser parser = new Parser();
			parser.setResource(_url);

			// Use a filter to extract title tags
			TagNameFilter filter = new TagNameFilter("title");
			NodeList nodeList = parser.extractAllNodesThatMatch(filter);

			// Extract the title from the first title tag
			if (nodeList.size() > 0) {
				TitleTag titleTag = (TitleTag) nodeList.elementAt(0);
				String title = titleTag.getTitle();

				System.out.println("Title: " + title);
			} else {
				System.out.println("Title: NA");
			}

		} catch (ParserException e) {
			System.out.println("Title: NA");
		}

		System.out.println("Url: " + _url);

		URL url;
		HttpURLConnection connection;

		try {
			url = new URL(_url);
			connection = (HttpURLConnection) url.openConnection();
		} catch (Exception e) {
			System.out.println("Last Modified: NA");
			System.out.println("Page Size:  NA");
			return;
		}

		try {
			Date lastModifiedDate = new Date(connection.getLastModified());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (!dateFormat.format(lastModifiedDate).equals("1970-01-01 08:00:00")) {
				System.out.println("Last Modified: " + dateFormat.format(lastModifiedDate));
			} else {
				System.out.println("Last Modified: NA");
			}
		} catch (Exception e) {
			System.out.println("Last Modified: NA");
		}

		try {
			int pageSize = connection.getContentLength();
			if (pageSize == -1) {
				String content = "";
				java.util.Scanner s = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A");
				content = s.hasNext() ? s.next() : "";
				pageSize = content.length();
				System.out.println("Page Size: " + pageSize + " characters");
			} else {
				System.out.println("Page Size: " + pageSize + " bytes");
			}
		} catch (Exception e) {
			System.out.println("Page Size: NA");
		}

		System.out.println("");
	}

	private void printWord(Vector<String> words, String _url) 
	{
		System.out.println("Words in "+ _url +" (size = "+words.size()+") :");
		for(int i = 0; i < words.size(); i++)
		{
			if(i<5 || i>words.size()-6){
				System.out.println(words.get(i));
			} else if(i==5){
				System.out.println("...");
			}
		}
		System.out.println("\n\n");
	}

	private HashMap<String, Integer> getProcessedWords(Vector<String> words) 
	{
		//stopword removal and stemming
		StopStem stopStem = new StopStem("stopwords.txt");
		HashMap<String, Integer> wordFrequency = new HashMap<>();
		for(int i = 0; i < words.size(); i++)
		{
			String word = words.get(i);
			if (stopStem.isStopWord(word))
			{
				//It should be stopped
			}
			else {
				stopStem.stem(word);
				// Update word frequency
				if (wordFrequency.containsKey(word)) {
					int count = wordFrequency.get(word);
					wordFrequency.put(word, count + 1);
				} else {
					wordFrequency.put(word, 1);
				}
			}
		}
		return wordFrequency;
	}

	private List<Map.Entry<String, Integer>> sortWordFrequency(HashMap<String, Integer> wordFrequency) {
		List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(wordFrequency.entrySet());

		// Sort the list in descending order based on the word frequencies
		Collections.sort(sortedList, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
				return entry2.getValue().compareTo(entry1.getValue());
			}
		});
		
		return sortedList;
	}

	private void printWordFrequency(List<Map.Entry<String, Integer>> sortedWordFrequency) {
		int i = 0;
		for (Map.Entry<String, Integer> entry : sortedWordFrequency) {
			if (i < maxDisplay) {
				System.out.print(entry.getKey() + ": " + entry.getValue() + "; ");
			} else if (i == maxDisplay) {
				System.out.println("...");
				break;
			}
			i++;
		}
		System.out.println("");
	}

	public void printChildLinks(Vector<String> links) 
	{
		for(int i = 0; i < links.size(); i++)	
		{
			if (i < maxDisplay) {
				System.out.println("Child " + links.get(i));
			} else if(i == maxDisplay){
				System.out.println("...");
				break;
			}
		}	
		System.out.println("");
	}

    public void recursiveExtractLinks() throws ParserException
	{
		//DFSfetchPage(url, maxDepth);

		BFSfetchPage(url, maxDepth);

		printAllLinks();
	}

	private void printAllLinks() 
	{
		System.out.println("All links: ");
		int i = 0;
		for (String link : visitedLinks) {
			System.out.println(++i + " : " + link);
		}
	}
	
	public static void main (String[] args)
	{
		try
		{
			Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 10, 30, 10);

			spider.recursiveExtractLinks();
		}
		catch (ParserException e)
		{
			e.printStackTrace();
		}

	}
}

	
