package jet.nsi.api.model;

import java.io.Serializable;

public class MetaOwn implements Serializable {

    private static final long serialVersionUID = 1L;

    private String attr;

    public MetaOwn() {
    }

    public MetaOwn(String attr) {
        super();
        this.attr = attr;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        
        if (this == obj)
            return true;
        
        if (! (obj instanceof MetaOwn))
            return false;
        
        MetaOwn another = (MetaOwn) obj;
        if (null == attr) {
            if (null == another.getAttr())
                return true;
            else
                return false;
        }
        
        return attr.equals(another.getAttr());
    }
}
