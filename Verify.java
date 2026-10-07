import java.io.File;
import java.io.IOException;

public class Verify {
    public static void main(String[] args) {
        GitInitialize git = new GitInitialize();
        // Recreating git should not reinitialize the repository
        git = new GitInitialize();
        try {
            System.out.println("SHA-1 of Verify.txt is 9a26e18fbc5b2435f4cabb1e91b6e780bb0d8e4d");
            git.createBlob("Verify.txt");
            git.updateIndex("Verify.txt");
            git.createBlob("test/awesome.txt");
            git.updateIndex("test/awesome.txt");
            git.createBlob("test/Hello.txt");
            git.updateIndex("test/Hello.txt");
            git.updateIndex("test/awesome.txt");
            System.out.println("== EMPTY ==");
            git.createBlob("empty.txt");
            git.updateIndex("empty.txt");
        } catch (Exception e) {
            System.out.println("File error: " + e.getMessage());
        }

    }
}
