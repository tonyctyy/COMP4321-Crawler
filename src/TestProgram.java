public class TestProgram {
    public static void main(String[] args) {
        String startUrl = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        int max_pages = 10;
        CrawlandIndex crawler = new CrawlandIndex(startUrl, max_pages);
        crawler.crawlAndIndexPages();
        crawler.displayDB();
        // crawler.outputFile();
    }
}
