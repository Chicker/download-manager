package ru.chicker;

import javaslang.control.Either;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    private final Collection<DownloadLinkInfo> links;
    private final int numThreads;
    private final long limitSpeed;
    private final String outputFolderName;

    public DownloadManager(Collection<DownloadLinkInfo> links, int numThreads,
                           long limitSpeed, String outputFolderName) {
        this.links = links;
        this.numThreads = numThreads;
        this.limitSpeed = limitSpeed;
        this.outputFolderName = outputFolderName;
    }

    public Collection<Either<DownlodError, DownloadSuccess>> start()
    throws InterruptedException {
        Queue<DownloadTask> taskList = new ConcurrentLinkedQueue<>();
        Queue<Either<DownlodError, DownloadSuccess>> resultList = new
            ConcurrentLinkedQueue<>();

        // Общее ограничение скорости делим на количество worker's
        int downloaderLimitSpeed = (int) (limitSpeed / numThreads);

        for (DownloadLinkInfo link : links) {
            taskList.add(new DownloadTask(link, outputFolderName));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executorService.submit(new Downloader(taskList, resultList,
                downloaderLimitSpeed));
        }

        executorService.shutdown();
        // TODO придумать что сделать timeout
        boolean timeoutOccurs = executorService.awaitTermination(30,
            TimeUnit.MINUTES);

        return resultList;
    }
}
