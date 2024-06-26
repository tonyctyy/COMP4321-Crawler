import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.StandardCopyOption;


public class TestProgram {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        String startUrl = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        int max_pages = 300;
        CrawlandIndex crawler = new CrawlandIndex(startUrl, max_pages);
        crawler.crawlAndIndexPages();
        // crawler.displayDB();
        // crawler.outputFile();

        // Source file path
        Path sourceFile = Paths.get("../data/database.db");
        // Destination directory path
        Path destinationDir = Paths.get("./apache-tomcat-10.1.20/webapps/comp4321/WEB-INF/database/");

        try {
            // Create the destination directory if it doesn't exist
            Files.createDirectories(destinationDir);
            // Copy the source file to the destination directory
            // if the file already exists, it will be replaced with the new one
            Files.copy(sourceFile, destinationDir.resolve(sourceFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            // Handle IO exception
            e.printStackTrace();
        }
        // count the time used to extract words
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        // convert the duration to second 
        double seconds = (double)duration / 1_000_000_000.0;

        System.out.println("Time used : " + seconds + " seconds");
    }
}
