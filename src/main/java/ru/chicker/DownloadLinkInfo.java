package ru.chicker;

public class DownloadLinkInfo {
    private final String fileName;

    private final String httpLink;

    public DownloadLinkInfo(String 
        fileName, String link) {
        this.fileName = fileName;
        this.httpLink = link;
    }

    public String getFileName() {
        return fileName;
    }

    public String getHttpLink() {
        return httpLink;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloadLinkInfo) {
            return equals((DownloadLinkInfo)obj);
        } else {
            return super.equals(obj);
        }
    }

    private boolean equals(DownloadLinkInfo other) {
        return this.fileName.equals(other.getFileName());
    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }

    @Override
    public String toString() {
        return "DownloadLinkInfo{" +
            "fileName='" + fileName + '\'' +
            ", httpLink='" + httpLink + '\'' +
            '}';
    }
}
