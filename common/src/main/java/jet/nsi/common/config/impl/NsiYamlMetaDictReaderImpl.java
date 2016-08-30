package jet.nsi.common.config.impl;

import java.io.InputStream;

import jet.nsi.api.NsiMetaDictReader;
import jet.nsi.api.model.MetaDict;
import jet.nsi.common.yaml.JaxbConstructor;

import org.yaml.snakeyaml.Yaml;

public class NsiYamlMetaDictReaderImpl implements NsiMetaDictReader {

    @Override
    public MetaDict read(InputStream src) {
        Yaml reader = new Yaml(new JaxbConstructor(MetaDict.class,getClass().getClassLoader()));
        return (MetaDict)reader.load(src);
    }

}
