<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ page import="IRUtilities.*" %>
<%@ page import="jdbm.RecordManager" %>
<%@ page import="jdbm.RecordManagerFactory" %>
<%@ page import="jdbm.htree.HTree" %>
<%@ page import="jdbm.helper.FastIterator" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>

<%
    Integer numKeyword = Integer.parseInt(request.getParameter("numKeyword"));
    Boolean useFreq = Boolean.parseBoolean(request.getParameter("freq").trim());

    //System.out.println(numKeyword);
    //System.out.println(useFreq);

    // here is the part for database
    String dbPath = getServletContext().getRealPath("/WEB-INF/database/database");

    RecordManager recman = RecordManagerFactory.createRecordManager(dbPath);

    long WordMappingID = recman.getNamedObject("WordMapping");
    HTree WordMapping = HTree.load(recman, WordMappingID);

    long InvertedBodyWordID = recman.getNamedObject("InvertedBodyWord");
    HTree InvertedBodyWord = HTree.load(recman, InvertedBodyWordID);

    long InvertedTitleWordID = recman.getNamedObject("InvertedTitleWord");
    HTree InvertedTitleWord = HTree.load(recman, InvertedTitleWordID);

    Map<String, Integer> frequencyMap = new HashMap<>();
    Map<String, Double> tfidfMap = new HashMap<>();
    Map<String, Double> freqXtfidfMap = new HashMap<>();

    FastIterator WordMappingKeys = WordMapping.keys();
    String key;

    while ((key = (String) WordMappingKeys.next()) != null) {
        String wordID = WordMapping.get(key).toString();
        String BodyValue = (String) InvertedBodyWord.get(wordID);
        String TitleValue = (String) InvertedTitleWord.get(wordID);
        // System.out.println(key + ": " + wordID);

        Integer frequency = 0;
        double tfidf = 0.0;
        double freqXtfidf = 0.0;

        if (BodyValue != null) {
            String [] BodyValues = BodyValue.split(",");
            for (String val : BodyValues) {
                String[] parts = val.split("\\|");
                frequency += Integer.parseInt(parts[1]);
                tfidf += Double.parseDouble(parts[2]);
                freqXtfidf += Integer.parseInt(parts[1]) * Double.parseDouble(parts[2]);
            }
        }
        
        if (TitleValue != null) {
            String [] TitleValues = TitleValue.split(",");
            for (String val : TitleValues) {
                String[] parts = val.split("\\|");
                frequency += Integer.parseInt(parts[1]);
                tfidf += Double.parseDouble(parts[2]);
                freqXtfidf += Integer.parseInt(parts[1]) * Double.parseDouble(parts[2]);
            }
        }
        frequencyMap.put(key, frequency);
        tfidfMap.put(key, tfidf);
        freqXtfidfMap.put(key, freqXtfidf);
        //System.out.println(key + ": " + frequencyMap.get(key));
        // System.out.println(tfidfMap.get(key));
    }

    // String wordID = WordMapping.get("interspers archiv").toString();
    // String BodyValue = (String) InvertedBodyWord.get(wordID);
    // String TitleValue = (String) InvertedTitleWord.get(wordID);
    // System.out.println(BodyValue);
    // System.out.println(TitleValue);
    // System.out.println(frequencyMap.get("interspers archiv"));
    // System.out.println(tfidfMap.get("interspers archiv"));

    // System.out.println("-----------------------------------------------------");

    StringBuilder json = new StringBuilder();
    json.append("{\"keywords\":[");

    Integer count = 0;

    if (useFreq) {
        List<Map.Entry<String, Double>> freqXtfidfList = new ArrayList<>(freqXtfidfMap.entrySet());
        freqXtfidfList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        //System.out.println(freqXtfidfList);

        // // Sort the frequencyMap by values in descending order
        // List<Map.Entry<String, Integer>> frequencyList = new ArrayList<>(frequencyMap.entrySet());
        // frequencyList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        // //System.out.println(frequencyList);
        for (Map.Entry<String, Double> entry : freqXtfidfList) {
            // System.out.println(entry.getKey() + ": " + entry.getValue());
            count++;
            if (count > numKeyword) {
                break;
            }
            String tempTxt = String.format("%.2f", entry.getValue()); 
            json.append("\"").append(entry.getKey()).append(": ").append(tempTxt).append("\",");
        }
        // List<Map.Entry<String, Integer>> topFrequencyList = frequencyList.subList(0, Math.min(frequencyList.size(), numKeyword));
        // System.out.println(topFrequencyList);
    } else {
        // Sort the tfidfMap by values in descending order
        List<Map.Entry<String, Double>> tfidfList = new ArrayList<>(tfidfMap.entrySet());
        tfidfList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        for (Map.Entry<String, Double> entry : tfidfList) {
            // System.out.println(entry.getKey() + ": " + entry.getValue());
            count++;
            if (count > numKeyword) {
                break;
            }
            String tempTxt = String.format("%.2f", entry.getValue()); 
            json.append("\"").append(entry.getKey()).append(": ").append(tempTxt).append("\",");
        }
        // List<Map.Entry<String, Double>> topTfidfList = tfidfList.subList(0, Math.min(tfidfList.size(), numKeyword));
        // System.out.println(topTfidfList);
    }   

    // Remove the trailing comma if any
    if (json.charAt(json.length() - 1) == ',') {
        json.deleteCharAt(json.length() - 1);
    }

    json.append("]}");

    // Set response content type to JSON
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    //System.out.println(json.toString());

    // Write JSON data to response
    out.print(json.toString());
%>