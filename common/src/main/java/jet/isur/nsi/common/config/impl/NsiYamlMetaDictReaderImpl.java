package jet.isur.nsi.common.config.impl;

import java.io.InputStream;

import jet.isur.nsi.api.NsiMetaDictReader;
import jet.isur.nsi.api.model.MetaDict;
import jet.isur.nsi.common.yaml.JaxbConstructor;

import org.yaml.snakeyaml.Yaml;

public class NsiYamlMetaDictReaderImpl implements NsiMetaDictReader {

    @Override
    public MetaDict read(InputStream src) {
        Yaml reader = new Yaml(new JaxbConstructor(MetaDict.class,getClass().getClassLoader()));
        return (MetaDict)reader.load(src);
    }

}
