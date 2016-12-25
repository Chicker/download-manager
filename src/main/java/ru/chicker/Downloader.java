package ru.chicker;

import javaslang.control.Either;
import javaslang.control.Try;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;


public class Downloader extends Thread {
    private static final int BUFFER_SIZE = 64 * 1024; // 64 Kb
    private static final int ONE_SECOND = 1000;

    private final Logger log = LoggerFactory.getLogger(Downloader.class);

    private final Queue<DownloadTask> taskList;
    private final Queue<Either<DownlodError, DownloadSuccess>> resultList;
    private final int speedLimit;

    public Downloader(Queue<DownloadTask> taskList,
                      Queue<Either<DownlodError, DownloadSuccess>> resultList, int speedLimit) {
        this.taskList = taskList;
        this.resultList = resultList;
        this.speedLimit = speedLimit;
    }

    @Override
    public void run() {
        StreamSupport.stream(new TaskQueueSpliterator(taskList), false)
            .map(task ->
                downloadLink(task)
                    .map(x -> new DownloadSuccess(task.getLinkInfo(), x))
                    .mapLeft(th -> new DownlodError(task.getLinkInfo(), th))
            )
            .forEach(resultList::add);
    }

    private ByteArrayOutputStream readWithLimit(int limit, InputStream inputStream)
    throws InterruptedException, IOException {

        int bytesRead = 0;
        // скачиваем за раз сколько позволено или размер буфера
        int bytesToReadOnce = limit <= BUFFER_SIZE ? limit : BUFFER_SIZE;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFFER_SIZE);

        do {
            long timeStart = System.currentTimeMillis();
            long sumBytesReadToLimit = 0;

            do {
                log.debug("Загрузчик начал закачку очередной порции данных");

                bytesRead = readAndWriteBuffer(inputStream, outputStream,
                    bytesToReadOnce);

                if (bytesRead == -1) break;

                sumBytesReadToLimit += bytesRead;

                // Если поток успел скачать отведенное ему кол-во байт меньше, чем за
                // 1 с, то на оставщееся время, он засыпает, чтобы не превысить 
                // заданное ограничение скорости
                long timeEnd = System.currentTimeMillis();
                long diff = timeEnd - timeStart;

                // если время еще не вышло (1 с)
                if (diff < ONE_SECOND) {
                    // и время на закачку еще есть, то идем дальше
                    if (sumBytesReadToLimit < limit) {
                        // ничего не делаем, идем качать следующую порцию данных
                    } else {
                        // мы достигли лимита скачанных байт за отведенное время
                        log.debug("Загрузчик достиг лимита скачанных байт за " +
                            "{} мс. Засыпает", diff);
                        Thread.sleep(ONE_SECOND - diff);
                    }

                } else {
                    // время вышло, начинаем заново
                    break;
                }
            } while (true);
        } while (bytesRead != -1);
        return outputStream;
    }

    private int readAndWriteBuffer(InputStream inputStream,
                                   ByteArrayOutputStream output,
                                   int size)
    throws IOException {
        int bytesRead;

        byte[] buffer = new byte[BUFFER_SIZE];
        bytesRead = inputStream.read(buffer, 0, size);

        if (bytesRead != -1) {
            log.debug("Загрузчик прочитал {} байт", bytesRead);
            output.write(buffer, 0, bytesRead);
        }

        return bytesRead;
    }

    private Either<Throwable, Long> downloadLink(DownloadTask downloadTask) {

        return Try.of(() -> {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(downloadTask.getLinkInfo().getHttpLink());
            long bytesRead = 0;
            log.info("Приступаю к загрузке {}", downloadTask.getLinkInfo().getFileName());

            CloseableHttpResponse response = httpclient.execute(httpget);
            bytesRead = 0;
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream instream = entity.getContent();
                        try {
                            ByteArrayOutputStream byteStream = readWithLimit(speedLimit,
                                instream);
                            saveToFile(downloadTask.getOutputFolder(), downloadTask
                                    .getLinkInfo().getFileName(),
                                byteStream);
                            bytesRead = byteStream.size();

                        } finally {
                            instream.close();
                        }
                    }
                } else {
                    throw new IOException(response.getStatusLine().toString());
                }

            } finally {
                response.close();
            }

            log.info("Загрузка {} завершена", downloadTask.getLinkInfo().getFileName());
            return bytesRead;
        }).toEither();
    }

    private void saveToFile(String outputFolder, String outputFile,
                            ByteArrayOutputStream
                                byteStream)
    throws IOException {
        Path outputFilePath = FileSystems.getDefault().getPath(outputFolder,
            outputFile);
        FileOutputStream fo = new FileOutputStream(outputFilePath.toFile());
        try {
            fo.write(byteStream.toByteArray());
            log.debug("Загрузчик сохранил закачку в папку {}", outputFilePath);
        } finally {
            fo.close();
        }
    }


    private final class TaskQueueSpliterator
        implements Spliterator<DownloadTask> {
        private final Queue<DownloadTask> taskList;

        TaskQueueSpliterator(Queue<DownloadTask> taskList) {
            this.taskList = taskList;
        }

        @Override
        public boolean tryAdvance(Consumer<? super DownloadTask> action) {
            DownloadTask task = taskList.poll();
            if (task != null) {
                action.accept(task);
                return true;
            } else return false;
        }

        @Override
        public Spliterator<DownloadTask> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }
}
