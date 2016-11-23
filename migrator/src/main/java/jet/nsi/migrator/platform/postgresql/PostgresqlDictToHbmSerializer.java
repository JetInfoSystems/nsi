package jet.nsi.migrator.platform.postgresql;

import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmColumnType;

import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.migrator.platform.DefaultDictToHbmSerializer;

public class PostgresqlDictToHbmSerializer
        extends DefaultDictToHbmSerializer {
    
    private final boolean useSequenceAsDefaultValueForId;
    
    public PostgresqlDictToHbmSerializer(boolean useSequenceAsDefaultValueForId) {
        this.useSequenceAsDefaultValueForId = useSequenceAsDefaultValueForId;
    }
    public PostgresqlDictToHbmSerializer() {
        this(false);
    }
    
    protected JaxbHbmColumnType buildIdColumn(NsiConfigField field, NsiConfigDict dict) {
        JaxbHbmColumnType result = buildColumn(field);
        if (useSequenceAsDefaultValueForId) {
            result.setDefault("nextval('" + dict.getSeq() + "')");
        }
        return result;
    }
}
