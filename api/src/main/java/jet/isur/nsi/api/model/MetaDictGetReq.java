package jet.isur.nsi.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="metaDictGetReq")
public class MetaDictGetReq extends BaseReq {

    private static final long serialVersionUID = 1L;
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

}
