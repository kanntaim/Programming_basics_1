package n.galeev;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        String pathToFolder;
        String extension;
        LinkedList<File> collectedFiles;
        File path;
        System.out.println("Enter directory path");
        while (true)
            try {
                pathToFolder = scanner.nextLine();
                path = new File(pathToFolder);
                if (!path.isDirectory()) {
                    System.out.println("Directory doesn't exist");
                    System.out.println("Enter correct directory path");
                    continue;
                }
                break;
            } catch (Exception e) {
                System.out.println("Wrong directory path");
                System.out.println("Enter correct directory path");
            }

        System.out.println("Enter extension");
        while (true) {
            try {
                extension = scanner.nextLine();
                if (extension.substring(0, 1).equals(".")) {
                    extension = extension.substring(1);
                }
                FileCollector collector = new FileCollector(extension);
                collectedFiles = collector.getLastCreatedFiles(path);
                break;
            } catch (Exception e) {
                System.out.println("Incorrect extension");
                System.out.println("Enter correct extension");
                return;
            }
        }

        try {
            System.out.println(collectedFiles.getFirst());
        }
        catch (NoSuchElementException e){
            System.out.println("There is no any files of this extension");
            return;
        }
        collectedFiles.removeFirst();
        if (collectedFiles.size()==0){
            return;
        }
        String[] collectedFilesString = new String[collectedFiles.size()];
        int i = 0;
        for(File node: collectedFiles){
           collectedFilesString[i] = (node.toString().substring(pathToFolder.length()));
           i++;
        }
        System.out.print(Arrays.toString(collectedFilesString));
    }

}

class FileCollector implements FileFilter{
    private static LinkedList<File> collectedFiles;
    private static String collectedFilesExtension;

    FileCollector(String extension){
        collectedFiles = new LinkedList<>();
        collectedFilesExtension = extension;
    }

    private void Collect(File path){
        LinkedList<File> folders = getAllFolders(path);
        for(File folder: folders) {
            File[] filesFromFolder = folder.listFiles(this);
            if (filesFromFolder != null && filesFromFolder.length !=0) collectedFiles.addAll(Arrays.asList(filesFromFolder));
        }

    }

    private LinkedList<File> getAllFolders(File folder){
        File[] filesList= folder.listFiles();
        LinkedList<File> nestedFolders = new LinkedList<>();
        if(filesList != null && filesList.length !=0){
            for(File file: filesList){
                if (file.isDirectory()){
                    nestedFolders.add(file);
                    LinkedList<File> filesInFolder = getAllFolders(file);
                    nestedFolders.addAll(filesInFolder);

                }
            }
        }
        return nestedFolders;
    }

    public LinkedList<File> getLastCreatedFiles(File path) throws Exception{
        Collect(path);
        FileTime lastCreationDate = null;
        LinkedList<File> lastCreatedFile = new LinkedList<>();
        for (File file : collectedFiles) {
            BasicFileAttributes attributes = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
            FileTime time = attributes.creationTime();
            if (lastCreationDate == null || time.compareTo(lastCreationDate) > 0) {
                lastCreationDate = time;
            }
        }
        for (File file : collectedFiles) {
            BasicFileAttributes attributes = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
            FileTime time = attributes.creationTime();
            if (time.compareTo(lastCreationDate) == 0) {
                lastCreatedFile.add(file);
            }
        }
        for (File file : collectedFiles) {
            BasicFileAttributes attributes = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
            FileTime time = attributes.creationTime();
            if (lastCreationDate.toMillis() - time.toMillis() < 10000) {
                lastCreatedFile.add(file);
            }
        }
        return lastCreatedFile;
    }

    public boolean accept(File file){
        if (file.isDirectory()) return false;
        else if (file.getName().contains(".")){
            return file.getName().split("[.]")[1].equals(collectedFilesExtension);
        }
        else return collectedFilesExtension.equals("");
    }
}
