package jet.isur.nsi.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="metaDictListRes")
public class MetaDictListRes extends BaseRes {
    private static final long serialVersionUID = 1L;
    private List<MetaDictRef> data;

    public List<MetaDictRef> getData() {
        return data;
    }

    public void setData(List<MetaDictRef> data) {
        this.data = data;
    }

}
