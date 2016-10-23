package jet.nsi.common.config.impl;

import jet.nsi.api.NsiMetaDictWriter;
import jet.nsi.api.model.MetaDict;
import jet.nsi.common.yaml.JaxbRepresenter;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by akatkevich on 21.10.2016.
 */
public class NsiYamlMetaDictWriterImpl implements NsiMetaDictWriter {

    @Override
    public void write (MetaDict dict, FileWriter fileWriter) {
        try {
            fileWriter.write(new Yaml(new JaxbRepresenter()).dumpAsMap(dict));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
