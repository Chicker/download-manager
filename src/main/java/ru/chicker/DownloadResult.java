package ru.chicker;

import ru.chicker.util.ExceptionUtils;

public class DownloadResult {
    private final DownloadLinkInfo linkInfo;
    private final long bytesCount;
    private final boolean success;
    private final Exception error;

    public DownloadResult(DownloadLinkInfo linkInfo, long bytesCount,
                          boolean success, Exception error) {
        this.linkInfo = linkInfo;
        this.bytesCount = bytesCount;
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getDownloadedFileSize() {
        return bytesCount;
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return String.format("Закачка файла [%s] размером [%d] байт " +
                "успешно " +
                "завершена.", linkInfo.getFileName(), bytesCount);
        } else {
            return String.format("Закачка файла [%s] завершилась ошибкой: " +
                "[%s]", linkInfo.getFileName(), ExceptionUtils.getCause
                (error).getLocalizedMessage());
        }
    }
}
