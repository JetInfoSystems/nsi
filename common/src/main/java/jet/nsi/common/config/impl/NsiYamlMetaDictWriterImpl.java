package jet.nsi.common.config.impl;

import jet.nsi.api.NsiMetaDictWriter;
import jet.nsi.api.model.MetaDict;
import jet.nsi.common.yaml.JaxbRepresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by akatkevich on 21.10.2016.
 */
public class NsiYamlMetaDictWriterImpl implements NsiMetaDictWriter {
    private static final Logger log = LoggerFactory.getLogger(NsiYamlMetaDictWriterImpl.class);

    @Override
    public void write (MetaDict dict, Writer fileWriter) {
        try {
            fileWriter.write(new Yaml(new JaxbRepresenter()).dumpAsMap(dict));
        } catch (IOException e) {
            log.error("write [{}] -> cant write dict", dict.getName(), e);
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                log.error("write can't close file", e);
            }
        }
    }

}
