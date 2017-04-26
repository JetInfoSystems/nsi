package jet.nsi.api.data;

import jet.nsi.api.model.MetaFieldType;

public class NsiConfigParams {
    private String lastUserDict;
    private String defaultVersionName = "version";
    private MetaFieldType defaultVersionType = MetaFieldType.NUMBER;
    private int defaultVersionSize = 6;
    private String defaultDeleteMarkName = "is_deleted";
    private MetaFieldType defaultDeleteMarkType = MetaFieldType.BOOLEAN;
    private int defaultDeleteMarkSize = 1;
    private String defaultIdName = "id";
    private MetaFieldType defaultIdType = MetaFieldType.NUMBER;
    private int defaultIdSize = 19;
    private String defaultIsGroupName = "is_group";
    private MetaFieldType defaultIsGroupType = MetaFieldType.BOOLEAN;
    private int defaultIsGroupSize = 1;
    private String defaultParentName = "parent_id";
    private String defaultLastChangeName = "last_change";
    private MetaFieldType defaultLastChangeType = MetaFieldType.DATE_TIME;
    private int defaultLastChangeSize = 0;
    private String defaultLastUserName = "last_user";
    private MetaFieldType defaultLastUserType = MetaFieldType.VARCHAR;
    private int defaultLastUserSize = 255;
    private String defaultOwnershipIdName = "ownership_id";
    private MetaFieldType defaultOwnershipIdType = MetaFieldType.NUMBER;
    private int defaultOwnershipIdSize = 19;

    private String defaultUserRef = "SYS_USERS";

    public String getDefaultOwnershipIdName() {
        return defaultOwnershipIdName;
    }

    public void setDefaultOwnershipIdName(String defaultOwnershipIdName) {
        this.defaultOwnershipIdName = defaultOwnershipIdName;
    }

    public MetaFieldType getDefaultOwnershipIdType() {
        return defaultOwnershipIdType;
    }

    public void setDefaultOwnershipIdType(MetaFieldType defaultOwnershipIdType) {
        this.defaultOwnershipIdType = defaultOwnershipIdType;
    }

    public int getDefaultOwnershipIdSize() {
        return defaultOwnershipIdSize;
    }

    public void setDefaultOwnershipIdSize(int defaultOwnershipIdSize) {
        this.defaultOwnershipIdSize = defaultOwnershipIdSize;
    }

    public String getDefaultUserRef() {
        return defaultUserRef;
    }

    public void setDefaultUserRef(String defaultUserRef) {
        this.defaultUserRef = defaultUserRef;
    }

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
