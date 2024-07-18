package http;

public class File {
    private final String name;
    private final byte[] content;
    private String uploadPath = "/static/upload/";
    private String imageSrc = "/upload/";
    private String uploadName = "";

    public File(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

    public String getUploadName() {
        return uploadName;
    }

    public void setUploadName(String uploadName) {
        this.uploadName = uploadName;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }
}
