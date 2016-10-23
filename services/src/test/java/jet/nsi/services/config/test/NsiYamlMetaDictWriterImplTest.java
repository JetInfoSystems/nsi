package jet.nsi.services.config.test;

import jet.nsi.api.model.MetaDict;
import jet.nsi.common.config.impl.NsiYamlMetaDictReaderImpl;
import jet.nsi.common.config.impl.NsiYamlMetaDictWriterImpl;
import jet.nsi.testkit.utils.DataUtils;
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

/**
 * Created by akatkevich on 21.10.2016.
 */
public class NsiYamlMetaDictWriterImplTest {
  @Test
    public void testWriteMetaDictImpl()  throws Exception {
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
}