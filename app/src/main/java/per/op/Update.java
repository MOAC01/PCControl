package per.op;

public class Update {

    private String versionName,updatePlain,resourceUri;
    private int versionCode;

    public Update(String versionName, String updatePlain, String resourceUri, int versionCode) {
        this.versionName = versionName;
        this.updatePlain = updatePlain;
        this.resourceUri = resourceUri;
        this.versionCode = versionCode;
    }

    public Update() {
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getUpdatePlain() {
        return updatePlain;
    }

    public void setUpdatePlain(String updatePlain) {
        this.updatePlain = updatePlain;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
}
