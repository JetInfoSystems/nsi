package jet.isur.nsi.api.data;

import jet.isur.nsi.api.model.MetaFieldType;

public class NsiConfigParams {
    private String lastUserDict;
    private String defaultVersionName = "VERSION";
    private MetaFieldType defaultVersionType = MetaFieldType.NUMBER;
    private int defaultVersionSize = 6;
    private String defaultDeleteMarkName = "IS_DELETED";
    private MetaFieldType defaultDeleteMarkType = MetaFieldType.BOOLEAN;
    private int defaultDeleteMarkSize = 1;
    private String defaultIdName = "ID";
    private MetaFieldType defaultIdType = MetaFieldType.NUMBER;
    private int defaultIdSize = 19;
    private String defaultIsGroupName = "IS_GROUP";
    private MetaFieldType defaultIsGroupType = MetaFieldType.BOOLEAN;
    private int defaultIsGroupSize = 1;
    private String defaultParentName = "PARENT_ID";
    private String defaultLastChangeName = "LAST_CHANGE";
    private MetaFieldType defaultLastChangeType = MetaFieldType.DATE_TIME;
    private int defaultLastChangeSize = 7;
    private String defaultLastUserName = "LAST_USER";
    private MetaFieldType defaultLastUserType = MetaFieldType.NUMBER;
    private int defaultLastUserSize = 19;
    

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

    public String getDefaultDeleteMarkName() {
        return defaultDeleteMarkName;
    }

    public void setDefaultDeleteMarkName(String defaultDeleteMarkName) {
        this.defaultDeleteMarkName = defaultDeleteMarkName;
    }

    public MetaFieldType getDefaultDeleteMarkType() {
        return defaultDeleteMarkType;
    }

    public void setDefaultDeleteMarkType(MetaFieldType defaultDeleteMarkType) {
        this.defaultDeleteMarkType = defaultDeleteMarkType;
    }

    public int getDefaultDeleteMarkSize() {
        return defaultDeleteMarkSize;
    }

    public void setDefaultDeleteMarkSize(int defaultDeleteMarkSize) {
        this.defaultDeleteMarkSize = defaultDeleteMarkSize;
    }

    public String getDefaultIdName() {
        return defaultIdName;
    }

    public void setDefaultIdName(String defaultIdName) {
        this.defaultIdName = defaultIdName;
    }

    public MetaFieldType getDefaultIdType() {
        return defaultIdType;
    }

    public void setDefaultIdType(MetaFieldType defaultIdType) {
        this.defaultIdType = defaultIdType;
    }

    public int getDefaultIdSize() {
        return defaultIdSize;
    }

    public void setDefaultIdSize(int defaultIdSize) {
        this.defaultIdSize = defaultIdSize;
    }

    public String getDefaultIsGroupName() {
        return defaultIsGroupName;
    }

    public void setDefaultIsGroupName(String defaultIsGroupName) {
        this.defaultIsGroupName = defaultIsGroupName;
    }

    public MetaFieldType getDefaultIsGroupType() {
        return defaultIsGroupType;
    }

    public void setDefaultIsGroupType(MetaFieldType defaultIsGroupType) {
        this.defaultIsGroupType = defaultIsGroupType;
    }

    public int getDefaultIsGroupSize() {
        return defaultIsGroupSize;
    }

    public void setDefaultIsGroupSize(int defaultIsGroupSize) {
        this.defaultIsGroupSize = defaultIsGroupSize;
    }

    public String getDefaultParentName() {
        return defaultParentName;
    }

    public void setDefaultParentName(String defaultParentName) {
        this.defaultParentName = defaultParentName;
    }

    public String getDefaultLastChangeName() {
        return defaultLastChangeName;
    }

    public void setDefaultLastChangeName(String defaultLastChangeName) {
        this.defaultLastChangeName = defaultLastChangeName;
    }

    public MetaFieldType getDefaultLastChangeType() {
        return defaultLastChangeType;
    }

    public void setDefaultLastChangeType(MetaFieldType defaultLastChangeType) {
        this.defaultLastChangeType = defaultLastChangeType;
    }

    public int getDefaultLastChangeSize() {
        return defaultLastChangeSize;
    }

    public void setDefaultLastChangeSize(int defaultLastChangeSize) {
        this.defaultLastChangeSize = defaultLastChangeSize;
    }

    public String getDefaultLastUserName() {
        return defaultLastUserName;
    }

    public void setDefaultLastUserName(String defaultLastUserName) {
        this.defaultLastUserName = defaultLastUserName;
    }

    public MetaFieldType getDefaultLastUserType() {
        return defaultLastUserType;
    }

    public void setDefaultLastUserType(MetaFieldType defaultLastUserType) {
        this.defaultLastUserType = defaultLastUserType;
    }

    public int getDefaultLastUserSize() {
        return defaultLastUserSize;
    }

    public void setDefaultLastUserSize(int defaultLastUserSize) {
        this.defaultLastUserSize = defaultLastUserSize;
    }
}
