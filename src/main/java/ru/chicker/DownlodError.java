package ru.chicker;

import ru.chicker.util.ExceptionUtils;

public class DownlodError {
    private final DownloadLinkInfo linkInfo;
    private final Throwable error;

    public DownlodError(DownloadLinkInfo linkInfo, Throwable error) {
        this.linkInfo = linkInfo;
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("Закачка файла [%s] завершилась ошибкой: " +
                "[%s]", linkInfo.getFileName(),
            ExceptionUtils.getCause(error).getLocalizedMessage());
    }
}
