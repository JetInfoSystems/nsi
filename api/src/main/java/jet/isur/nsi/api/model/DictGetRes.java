package jet.isur.nsi.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dicGetRes")
public class DictGetRes extends BaseRes {

    private static final long serialVersionUID = 1L;
    private DictRow data;
    public DictRow getData() {
        return data;
    }
    public void setData(DictRow data) {
        this.data = data;
    }

}
