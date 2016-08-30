package jet.nsi.generator.dictdata;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jet.nsi.generator.data.DataFiles;

public class DictDataFiles implements DataFiles {
    private static final Logger log = LoggerFactory.getLogger(DictDataFiles.class);

    private Collection<File> dictFiles;
    
    
    public DictDataFiles(File dictdataPath) {
        log.debug("dictdata path: " + dictdataPath.getPath());
        
        FileFilter filter = new WildcardFileFilter("*.json");
        dictFiles = Arrays.asList(dictdataPath.listFiles(filter));
    }

    @Override
    public Collection<File> getFiles(){
        return dictFiles;
    }
}
