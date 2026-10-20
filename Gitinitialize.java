import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HexFormat;

public class GitInitialize {

    private File gitFolder;
    private File objectsFolder;
    private File indexFile;
    private File headFile;

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
            // Reading in the entirety of the file and adding in new lines if the file
            // contents
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

            BufferedReader fileReader = new BufferedReader(new FileReader(indexFile.toPath().toString()));
            StringBuilder indexString = new StringBuilder();
            boolean replacedFile = false;
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                // For each line, we find the file path that is mapped on that line.
                // If that file path is the same as the path we currently have
                // We just rewrite the line
                if (line.substring(line.indexOf(" ") + 1).equals("myProject/" + filePath)) {
                    replacedFile = true;
                    indexString.append(hash + " myProject/" + filePath);
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
                    indexString.append(hash + " myProject/" + filePath);
                } else {
                    indexString.append("\n" + hash + " myProject/" + filePath);
                }

            }
            FileWriter fileWriter = new FileWriter(indexFile.toPath().toString());
            fileWriter.write(indexString.toString());

            fileReader.close();
            fileWriter.close();

        } catch (Exception e) {
            System.out.println("There's an error: " + e.getMessage());
        }
    }

    public String findLeafMostDirectory(File workingList) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(workingList));
        // while (fileReader.ready()) {
        // String listLine = fileReader.readLine();
        // if (listLine.indexOf("tree") == -1) {
        // // Blob file
        // int spaceIndex = listLine.lastIndexOf(" ");
        // int lastSlashLine = listLine.lastIndexOf("/");
        // fileReader.close();
        // return listLine.substring(spaceIndex + 1, lastSlashLine);
        // }
        // }
        String leafMost = "";
        while (fileReader.ready()) {
            String listLine = fileReader.readLine();
            int spaceIndex = listLine.lastIndexOf(" ");
            int lastSlashLine = listLine.lastIndexOf("/");
            if (lastSlashLine != -1) {
                if (listLine.substring(spaceIndex + 1, lastSlashLine).length() >= leafMost
                        .length()) {
                    leafMost = listLine.substring(spaceIndex + 1, lastSlashLine);
                }
            }
            // If lastSlashLine is -1, the only directory remaining is the root directory
        }
        fileReader.close();
        return leafMost;
    }

    public void rewriteWorkingList(File workingList, String treehash, String dir) throws IOException {
        // treeWritten will be made true when we overwrite one line with the tree hash
        // to ensure that we don't duplicate entries.
        boolean treeWritten = false;
        BufferedReader fileReader = new BufferedReader(new FileReader(workingList));
        StringBuilder workingListStringBuilder = new StringBuilder();
        while (fileReader.ready()) {
            String line = fileReader.readLine();
            int spaceIndex = line.lastIndexOf(" ");
            int slashIndex = line.lastIndexOf("/");
            if (line.substring(spaceIndex + 1, slashIndex).equals(dir) && !treeWritten) {
                treeWritten = true;
                workingListStringBuilder.append("\ntree " + treehash + " " + dir);
            } else if (!line.substring(spaceIndex + 1, slashIndex).equals(dir)) {
                workingListStringBuilder.append("\n" + line);
            }
        }
        fileReader.close();
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(workingList));
        fileWriter.write(workingListStringBuilder.toString().substring(1));
        fileWriter.close();
    }

    public String createTreesFromIndex() {
        try {
            File workingList = createWorkingList(indexFile);
            String leafMost = findLeafMostDirectory(workingList);
            while (leafMost != "") {
                System.out.println(leafMost);
                String treeHash = createTree(workingList, leafMost);
                rewriteWorkingList(workingList, treeHash, leafMost);
                leafMost = findLeafMostDirectory(workingList);
            }
            String rootHash = hashFile(workingList.getAbsolutePath());
            workingList.delete();
            return rootHash;
        } catch (Exception e) {
            System.out.println("File error: " + e.getMessage());
        }
        return "";
    }

    public File createWorkingList(File fileList) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(fileList));
        File workingList = new File("workingList");
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(workingList));
        // Add the lines into a list for later
        ArrayList<String> lines = new ArrayList<String>();
        while (fileReader.ready()) {
            String line = fileReader.readLine();
            lines.add(line);
            // Creating a comparator to compare by path. If you're wondering where I got
            // this from:
            // https://www.baeldung.com/java-comparator-comparable
            Comparator<String> byPath = (String s1, String s2) -> -s1.substring(s1.indexOf(" ") + 1)
                    .compareTo(s2.substring(s2.indexOf(" ") + 1));
            Collections.sort(lines, byPath);
        }
        for (int i = 0; i < lines.size(); i++) {
            fileWriter.write("blob " + lines.get(i));
            if (i != lines.size() - 1) {
                fileWriter.write("\n");
            }
        }
        fileReader.close();
        fileWriter.close();
        return workingList;
    }

    public String createTree(File workingList, String dirPath) {
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(workingList));
            ArrayList<String> fileList = new ArrayList<String>();
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                // Take the last slash we find because everything before that is the directory
                // path
                int lastSlashIndex = line.lastIndexOf("/");
                // Taking the index of the space to see where the directory path begins.
                int spaceIndex = line.lastIndexOf(" ");
                if (line.substring(spaceIndex + 1, lastSlashIndex).equals(dirPath)) {
                    fileList.add(line);
                }
            }
            fileReader.close();

            File newTree = new File("git/objects/newTree");
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter("git/objects/newTree"));
            for (int i = 0; i < fileList.size(); i++) {
                fileWriter.write(fileList.get(i));
                if (i < fileList.size() - 1) {
                    // This is the last file
                    fileWriter.write("\n");
                }
            }
            fileWriter.close();
            // Rehashing the tree now and changing name
            String hash = hashFile("git/objects/newTree");
            newTree.renameTo(new File("git/objects/" + hash));
            return hash;
        } catch (Exception e) {
            System.out.println("File error: " + e.getMessage());
        }
        return "";

    }
}
