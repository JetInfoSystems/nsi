package jet.isur.nsi.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dictListReq")
public class DictListReq extends BaseReq {

    private static final long serialVersionUID = 1L;
    private String objectType;
    private Integer offset;
    private Integer size;
    private DictRowAttr parent;
    private DictRowAttr owner;
    private BoolExp filter;
    private List<SortExp> sort;
    public String getObjectType() {
        return objectType;
    }
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
    public Integer getOffset() {
        return offset;
    }
    public void setOffset(Integer offset) {
        this.offset = offset;
    }
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public DictRowAttr getParent() {
        return parent;
    }
    public void setParent(DictRowAttr parent) {
        this.parent = parent;
    }
    public DictRowAttr getOwner() {
        return owner;
    }
    public void setOwner(DictRowAttr owner) {
        this.owner = owner;
    }
    public BoolExp getFilter() {
        return filter;
    }
    public void setFilter(BoolExp filter) {
        this.filter = filter;
    }
    public List<SortExp> getSort() {
        return sort;
    }
    public void setSort(List<SortExp> sort) {
        this.sort = sort;
    }

}
