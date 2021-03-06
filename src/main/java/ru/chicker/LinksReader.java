package ru.chicker;

import ru.chicker.exception.InvalidFileStructureException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class LinksReader {

    public Set<DownloadLinkInfo> load(String linksFileName)
    throws FileNotFoundException, InvalidFileStructureException {
        
        Set<DownloadLinkInfo> result = new HashSet<>();

        try (Scanner scanner = new Scanner(new FileInputStream(linksFileName))) {
            while (scanner.hasNextLine()) {
                try {
                    String httpLink = scanner.next();
                    String fileNameToSave = scanner.next();

                    result.add(new DownloadLinkInfo(fileNameToSave,
                        httpLink));
                } catch (NoSuchElementException ex) {
                    throw new InvalidFileStructureException(linksFileName, ex);
                }
            }
        }
        return result;
    }
}
