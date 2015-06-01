package jet.isur.nsi.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dicDeleteReq")
public class DictDeleteReq extends BaseReq {

    private static final long serialVersionUID = 1L;
    private DictRowAttr id;
    private Boolean value;
    public DictRowAttr getId() {
        return id;
    }
    public void setId(DictRowAttr id) {
        this.id = id;
    }
    public Boolean getValue() {
        return value;
    }
    public void setValue(Boolean value) {
        this.value = value;
    }

}
