package ru.chicker;

public class DownloadTask {
    private final DownloadLinkInfo linkInfo;
    private final String outputFolder;

    public DownloadTask(DownloadLinkInfo linkInfo, String outputFolder) {
        this.linkInfo = linkInfo;
        this.outputFolder = outputFolder;
    }

    public DownloadLinkInfo getLinkInfo() {
        return linkInfo;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    @Override
    public String toString() {
        return String.format("Закачка [%s]; выходная папка [%s]", linkInfo
            .toString(), outputFolder);
    }
}
