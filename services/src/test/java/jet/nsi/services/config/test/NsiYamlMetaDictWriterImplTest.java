package jet.nsi.services.config.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import jet.nsi.api.model.MetaDict;
import jet.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;
import jet.nsi.common.config.impl.NsiYamlMetaDictWriterImpl;
import jet.nsi.testkit.utils.DataUtils;

/**
 * Created by akatkevich on 21.10.2016.
 */
public class NsiYamlMetaDictWriterImplTest {
    @Test
    public void testWriteMetaDict()  throws Exception {
      /* Gen metadata */
      MetaDict o1 = DataGen.genMetaDict("dict1", "table1").build();
      File testFile = new File("src/test/resources/metadata1/dumpMeta.yaml");
      FileWriter fw = new FileWriter(testFile);

      NsiYamlMetaDictWriterImpl writer = new NsiYamlMetaDictWriterImpl();
      /* Dump metadata */
      writer.write(o1,fw);

      NsiYamlMetaDictReaderImpl reader = new NsiYamlMetaDictReaderImpl();
      try(InputStream stream = new FileInputStream(testFile)) {
          MetaDict o2 = reader.read(stream);
          DataUtils.assertEqualAllOptionals(o1, o2);
      }
      /* delete test file */
      testFile.delete();
    }
    
    @Test
    public void testWriteMetaDictWithLabels() throws IOException {
        MetaDict o1 = DataGen.genMetaDictWithLabels("dictWithLabels", "tableWithLabels").build();
        File testFile = new File("src/test/resources/metadata1/dumpMetaWithLabels.yaml");
        
        FileWriter fw = new FileWriter(testFile);

        NsiYamlMetaDictWriterImpl writer = new NsiYamlMetaDictWriterImpl();
        /* Dump metadata */
        writer.write(o1,fw);

        NsiYamlMetaDictReaderImpl reader = new NsiYamlMetaDictReaderImpl();
        try(InputStream stream = new FileInputStream(testFile)) {
            MetaDict o2 = reader.read(stream);
            DataUtils.assertEqualAllOptionals(o1, o2);
        }
        
        testFile.delete();
    }
}