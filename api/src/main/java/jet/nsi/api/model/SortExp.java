package jet.nsi.api.model;

import java.io.Serializable;

public class SortExp implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * Название параметра.
     */
    private String key;
    /**
     * Направление сортировки.
     */
    private Boolean asc;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public Boolean getAsc() {
        return asc;
    }
    public void setAsc(Boolean asc) {
        this.asc = asc;
    }

}
