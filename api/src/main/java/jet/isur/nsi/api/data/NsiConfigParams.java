package jet.isur.nsi.api.data;

import jet.isur.nsi.api.model.MetaFieldType;

public class NsiConfigParams {
    private String lastUserDict;
    private String defaultVersionName = "VERSION";
    private MetaFieldType defaultVersionType = MetaFieldType.NUMBER;
    private int defaultVersionSize = 6;

    public String getLastUserDict() {
        return lastUserDict;
    }

    public void setLastUserDict(String lastUserDict) {
        this.lastUserDict = lastUserDict;
    }

    public MetaFieldType getDefaultVersionType() {
        return defaultVersionType;
    }

    public void setDefaultVersionType(MetaFieldType defaultVersionType) {
        this.defaultVersionType = defaultVersionType;
    }

    public int getDefaultVersionSize() {
        return defaultVersionSize;
    }

    public void setDefaultVersionSize(int defaultVersionSize) {
        this.defaultVersionSize = defaultVersionSize;
    }

    public String getDefaultVersionName() {
        return defaultVersionName;
    }

    public void setDefaultVersionName(String defaultVersionName) {
        this.defaultVersionName = defaultVersionName;
    }
}
