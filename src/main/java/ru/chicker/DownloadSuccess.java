package ru.chicker;

public class DownloadSuccess {
    private final DownloadLinkInfo linkInfo;

    private final long bytesCount;

    public DownloadSuccess(DownloadLinkInfo linkInfo, long bytesCount) {
        this.linkInfo = linkInfo;
        this.bytesCount = bytesCount;
    }

    public long getDownloadedFileSize() {
        return bytesCount;
    }

    @Override
    public String toString() {
        return String.format("Закачка файла [%s] размером [%d] байт " +
            "успешно " +
            "завершена.", linkInfo.getFileName(), bytesCount);
    }
}
