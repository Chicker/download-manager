package ru.chicker;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chicker.exception.InvalidFileStructureException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

public class Main {
    private static final int ONE_KILOBYTE = 1024;
    private static final int ONE_MEGABYTE = ONE_KILOBYTE * ONE_KILOBYTE;
    private static final int DEFAULT_DOWNLOADERS_NUMBER = 2;
    private static final int DEFAULT_LIMIT_SPEED = 1000 * ONE_MEGABYTE;
    private static final String APP_NAME = "download-manager";
    
    private static Logger log = LoggerFactory.getLogger(Main.class); 
    private static int nThreads = DEFAULT_DOWNLOADERS_NUMBER;
    private static int limitSpeed = DEFAULT_LIMIT_SPEED;
    private static String linksFile;
    private static String outputFolder;

    public static void main(String[] args) {
        Options cliOptions = getOptions();

        try {
            parseCommandLineArguments(args, cliOptions);

            checkFileExists(outputFolder);
            checkFileExists(linksFile);
            
            printHeader();

            Collection<DownloadResult> resultList = startDownloading();
            
            printFooter(resultList);

        } catch (ParseException e) {
            log.error("Ошибка запуска программы. {}", e.getLocalizedMessage());
            showUsage(cliOptions);
        } catch (InterruptedException | FileNotFoundException | InvalidFileStructureException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private static void printHeader() {
        System.out.println("----------------------");
        System.out.println("Параметры запуска программы:");
        System.out.println(String.format("  - Количество одновременно " +
            "качающих " +
            "потоков: %d", nThreads));
        System.out.println(String.format("  - Общее ограничение на скорость " +
            "скачивания: %s/sec", formatBytes(limitSpeed)));
        System.out.println(String.format("  - Путь к файлу со списком ссылок:" +
                " %s",
            linksFile));
        System.out.println(String.format("  - Имя папки, куда складывать " +
                "файлы: %s",
            outputFolder));
        System.out.println("----------------------");
    }

    private static void printFooter(Collection<DownloadResult> resultList) {
        long sizeOfAllDownloads = 0;
        System.out.println("----------------------");
        System.out.println("Статистика работы программы:");
        for (DownloadResult r : resultList) {
            sizeOfAllDownloads += r.getDownloadedFileSize();
            System.out.println(r.toString());
        }
        System.out.println(String.format("Всего скачано [%d] файлов, " +
            "общего размера: [%s]", resultList.size(), formatBytes
            (sizeOfAllDownloads)));
        System.out.println("----------------------");
    }

    private static String formatBytes(long bytes) {
        if (bytes < ONE_MEGABYTE) {
            return String.format("%.2f Kb", (float)bytes / ONE_KILOBYTE);
        } else {
            return String.format("%.2f Mb", (float)bytes / (ONE_MEGABYTE));
        }
        
    }

    private static void checkFileExists(String fileOrDirName) throws 
                                                   FileNotFoundException {
        File fileHandler = new File(fileOrDirName);
        if (!fileHandler.exists()) {
            throw new FileNotFoundException(String.format("Файл/папка [%s] " +
                "не существует или не доступна.",
                fileOrDirName));
        }
    }

    private static Collection<DownloadResult> startDownloading()
    throws InterruptedException, FileNotFoundException,
           InvalidFileStructureException {

        LinksReader linksReader = new LinksReader();
        Collection<DownloadLinkInfo> links = linksReader.load(linksFile);

        DownloadManager dm = new DownloadManager(links, nThreads, limitSpeed,
            outputFolder);
        
        return dm.start();
    }

    private static void parseCommandLineArguments(String[] args, Options
        cliOptions)
    throws ParseException {
        CommandLineParser cliParser = new DefaultParser();


        CommandLine line = cliParser.parse(cliOptions, args);

        if (line.hasOption("n")) {
            nThreads = Integer.parseUnsignedInt(line.getOptionValue("n"));
        }

        if (line.hasOption("l")) {
            String limitSpeedStrValue = line.getOptionValue("l");
            if (limitSpeedStrValue.endsWith("k")) {
                limitSpeed = Math.round(ONE_KILOBYTE * Float.parseFloat
                    (limitSpeedStrValue
                    .substring(0, limitSpeedStrValue.length() - 1)));
            } else if (limitSpeedStrValue.endsWith("m")) {
                limitSpeed = Math.round(ONE_MEGABYTE * Float.parseFloat
                    (limitSpeedStrValue
                    .substring(0, limitSpeedStrValue.length() - 1)));
            } else {
                limitSpeed = Integer.parseInt(limitSpeedStrValue);
            }
        }

        if (line.hasOption("f")) {
            linksFile = line.getOptionValue("f");
        } else {
            throw new ParseException("Не указан обязательный параметр -f");
        }

        if (line.hasOption("o")) {
            outputFolder = line.getOptionValue("o");
        } else {
            throw new ParseException("Не указан обязательный параметр -o");
        }
    }

    private static void showUsage(Options cliOptions) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(APP_NAME, cliOptions);
    }

    private static Options getOptions() {
        Options cliOptions = new Options();

        cliOptions.addOption("n", true, "количество одновременно качающих " +
            "потоков");
        cliOptions.addOption("l", true, "общее ограничение на скорость " +
            "скачивания");
        cliOptions.addOption("f", true, "путь к файлу со списком ссылок");
        cliOptions.addOption("o", true, "имя папки, куда складывать скачанные" +
            " файлы");
        return cliOptions;
    }
}
