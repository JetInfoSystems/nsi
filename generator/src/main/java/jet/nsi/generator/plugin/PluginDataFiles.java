package jet.nsi.generator.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jet.nsi.generator.data.DataFiles;

public class PluginDataFiles implements DataFiles {
	private static final Logger log = LoggerFactory.getLogger(PluginDataFiles.class);

    Collection<File> files;
    
    
    public PluginDataFiles(File dataPath, Collection<String> fnames) {
        log.debug("plugin data path: " + dataPath.getPath());
        
        files = new ArrayList<>(fnames.size());
        for (String fname : fnames) {
            files.add(new File(dataPath, fname + ".json"));
        }
    }

    @Override
    public Collection<File> getFiles(){
        return files;
    }


}
