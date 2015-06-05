package jet.isur.nsi.api.data;

public class NsiQueryAttr {
    private final String alias;
    private final String refAlias;
    private final NsiConfigAttr attr;
    private final int index;

    public NsiQueryAttr(String alias, NsiConfigAttr attr, String refAlias, int index) {
        this.alias = alias;
        this.attr = attr;
        this.refAlias = refAlias;
        this.index = index;
    }

    public String getAlias() {
        return alias;
    }

    public String getRefAlias() {
        return refAlias;
    }

    public NsiConfigAttr getAttr() {
        return attr;
    }

    public int getIndex() {
        return index;
    }


}
