package jet.isur.nsi.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dictCountRes")
public class DictCountRes extends BaseRes {

    private static final long serialVersionUID = 1L;
    private Integer count;
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

}
