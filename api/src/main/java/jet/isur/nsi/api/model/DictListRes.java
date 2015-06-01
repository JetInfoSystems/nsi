package jet.isur.nsi.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dictListRes")
public class DictListRes extends BaseRes {
    private static final long serialVersionUID = 1L;
    private List<DictRow> data;

    public List<DictRow> getData() {
        return data;
    }

    public void setData(List<DictRow> data) {
        this.data = data;
    }

}
