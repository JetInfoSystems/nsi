package jet.isur.nsi.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="metaDictGetRes")
public class MetaDictGetRes extends BaseRes {

    private static final long serialVersionUID = 1L;
    private List<MetaDict> data;
    public List<MetaDict> getData() {
        return data;
    }
    public void setData(List<MetaDict> data) {
        this.data = data;
    }

}
