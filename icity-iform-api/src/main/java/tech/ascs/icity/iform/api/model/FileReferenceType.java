package tech.ascs.icity.iform.api.model;

public enum FileReferenceType {
    Image("Image"),//图片
    Video("Video"),//视频
    Attachment("Attachment");//附件

    private String value;

    private FileReferenceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
