import java.io.File;
import java.io.IOException;

public class Verify {
    public static void main(String[] args) {
        GitInitialize git = new GitInitialize();
        // Recreating git should not reinitialize the repository
        git = new GitInitialize();
        try {
            System.out.println("SHA-1 of Verify.txt is 566336e72a24c615f6c4f2f54a9e1267ee7a5da0");
            git.createBlob("Verify.txt");
            git.updateIndex("Verify.txt");
            git.createBlob("test/awesome.txt");
            git.updateIndex("test/awesome.txt");
        } catch (Exception e) {
            System.out.println("File error: " + e.getMessage());
        }

    }
}