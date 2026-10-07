import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

public class GitInitialize {

    private File gitFolder;
    private File objectsFolder;
    private File indexFile;
    private File headFile;

    public static void main(String[] args) {

        GitInitialize git = new GitInitialize();

        try {
            System.out.println(hashFile("Hello.txt"));
            git.createBlob("Hello.txt");
            git.updateIndex("Hello.txt");
        } catch (Exception e) {
            System.out.println("oops");
        }
    }

    public GitInitialize() {
        instantiate();
    }

    public void instantiate() {
        try {
            int filesThatDidntHaveToBeRecreated = 0;
            gitFolder = new File("git/");
            if (!gitFolder.mkdir()) {
                filesThatDidntHaveToBeRecreated++;
            }
            objectsFolder = new File(gitFolder, "objects/");
            if (!objectsFolder.mkdir()) {
                filesThatDidntHaveToBeRecreated++;
            }
            indexFile = new File(gitFolder, "index");
            if (!indexFile.createNewFile()) {
                filesThatDidntHaveToBeRecreated++;
            }
            headFile = new File(gitFolder, "Head");
            if (!headFile.createNewFile()) {
                filesThatDidntHaveToBeRecreated++;
            }
            if (filesThatDidntHaveToBeRecreated == 4) {
                System.out.println("Git Repository Already Exists");
            } else {
                System.out.println("Git Repository Created");
            }
        } catch (Exception e) {
            System.out.println("There's an error.");
        }

    }

    public static String hashFile(String filePath) throws IOException {
        // TODO (FH-4): read the whole file, digest it, convert the bytes to hex
        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            throw new IOException("no such file: " + filePath);
        }

        byte[] fileBytes = Files.readAllBytes(path);

        MessageDigest shaAlgorithm;

        try {
            shaAlgorithm = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }

        byte[] hashBytes = shaAlgorithm.digest(fileBytes);

        return HexFormat.of().formatHex(hashBytes);
    }

    public void createBlob(String filePath) throws IOException {
        try {

            String hash = hashFile(filePath);
            File newFile = new File(objectsFolder, hash);
            newFile.createNewFile();

            BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
            StringBuilder fileContents = new StringBuilder();
            // Reading in the entirety of the file and adding in new lines if the file contents
            // haven't ended.
            while (fileReader.ready()) {
                fileContents.append(fileReader.readLine());
                if (fileReader.ready()) {
                    fileContents.append("\n");
                }
            }
            fileReader.close();
            FileWriter fileWriter = new FileWriter(newFile.toPath().toString());
            fileWriter.write(fileContents.toString());
            fileWriter.close();


        } catch (Exception e) {
            System.out.println("There's an error in createBlob: " + e.getMessage());
        }
    }

    public void updateIndex(String filePath) throws IOException {
        try {
            String hash = hashFile(filePath);

            BufferedReader fileReader =
                    new BufferedReader(new FileReader(indexFile.toPath().toString()));
            StringBuilder indexString = new StringBuilder();
            boolean replacedFile = false;
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                // For each line, we find the file path that is mapped on that line.
                // If that file path is the same as the path we currently have
                // We just rewrite the line
                if (line.substring(line.indexOf(" ") + 1).equals(filePath)) {
                    replacedFile = true;
                    indexString.append(hash + " " + filePath);
                } else {
                    indexString.append(line);
                }
                if (fileReader.ready()) {
                    indexString.append("\n");
                }
            }
            if (!replacedFile) {
                // We did not replace a file so this file was never added to the index before
                if (indexString.toString().equals("")) {
                    indexString.append(hash + " " + filePath);
                } else {
                    indexString.append("\n" + hash + " " + filePath);
                }

            }
            // if (fileReader.readLine() == null) {
            // fileWriter.write(hash + " " + filePath);
            // } else {
            // fileWriter.write("\n" + hash + " " + filePath);
            // }
            // Contents of the new index file are now ready to be written in.
            FileWriter fileWriter = new FileWriter(indexFile.toPath().toString());
            fileWriter.write(indexString.toString());

            fileReader.close();
            fileWriter.close();

        } catch (Exception e) {
            System.out.println("There's an error: " + e.getMessage());
        }
    }
}
