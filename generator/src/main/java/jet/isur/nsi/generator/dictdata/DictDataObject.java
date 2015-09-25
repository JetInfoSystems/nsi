package jet.isur.nsi.generator.dictdata;

import jet.isur.nsi.generator.data.DataObject;

public class DictDataObject extends DataObject{

    private String parentFieldName;

    public String getParentFieldName() {
        return parentFieldName;
    }

    public void setParentFieldName(String parentFieldName) {
        this.parentFieldName = parentFieldName;
    }
}
