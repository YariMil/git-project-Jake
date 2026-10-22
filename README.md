### git-project-Jake
instantiate()
hashFile()
createBlob()

## GP-3.0 done by Yari:

# createWorkingList(File fileList)
Takes in a file the contents of which are a list of files in the style of the index. The method goes line by line using a BufferedReader, writing each line into a list which will be sorted later. The method then creates a simple comparator which compares two lines from the index by their file paths. The list is then sorted using that comparator and its contents are written into a new workingList file. Because all of the paths in the list are BLOBs, "blob" is attached when they are written to the working list to identify them as such.

# findLeafMostDirectory(File workingList)
Takes in a working list and goes through it finding the deepest directory in it. Returns the deepest directory

# createTree(File workingList, String dirPath)
Takes in a working list and the directory with which to create a tree object. The method goes through the working list and collects all the paths which share the same directory path as dirPath. A new file called in git/objects called newTree is created and all of the paths are written into it. The file is hashed and is renamed to that hash. The has is returned.

# rewriteWorkingList(File workingList, String treehash, String dir)
Takes in a working list, a hash of a tree, and its directory. Goes through the working and finds all instances of that directory path in the index. The first time this is the case, the line is rewritten to be the tree hash and the directory path, consolidating the working list entry under a tree object. All other instances are just not written as the tree object entry encloses all instances of the directory path. 

# createTreesFromIndex()
Utilizes a recursive loop to create trees from the index file. It first creates the working list. In the actual loop, it first finds the leaf most directory using findLeafMostDirectory, then creates a tree using createTree and that leaf most directory, and then rewrites the working list using rewriteWorkingList to consolidate all the instances of that leaf most directory under a singular tree entry. This continues until only the root directory remains, and its hash is returned.