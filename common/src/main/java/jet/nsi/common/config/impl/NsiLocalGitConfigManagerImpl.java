package jet.nsi.common.config.impl;

import jet.nsi.api.NsiConfigManager;
import jet.nsi.api.NsiMetaDictReader;
import jet.nsi.api.NsiMetaDictWriter;
import jet.nsi.api.data.NsiConfig;
import jet.nsi.api.data.NsiConfigParams;
import jet.nsi.api.model.MetaDict;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NsiLocalGitConfigManagerImpl implements NsiConfigManager {

    private static final Logger log = LoggerFactory.getLogger(NsiLocalGitConfigManagerImpl.class);

    private final class ConfigFileWalker extends DirectoryWalker<File> {
        private ConfigFileWalker(FileFilter filter) {
            super(filter, -1);
        }

        @Override
        protected void handleFile(File file, int depth, Collection<File> results)
                throws IOException {
            results.add(file);
        }

        Set<File> find(File configPath) {
            Set<File> result = new HashSet<>();
            try {
                walk(configPath, result);
                return result;
            } catch( Exception e) {
                throw new NsiConfigException("find config files",e);
            }
        }
    }

    private final File configPath;
    private final NsiMetaDictReader reader;
    private final NsiMetaDictWriter writer;
    private final NsiConfigParams configParams;
    private NsiConfigImpl config;

    public NsiLocalGitConfigManagerImpl(File configPath, NsiMetaDictReader reader, NsiConfigParams configParams) {
        this.configPath = configPath;
        this.reader = reader;
        this.writer = null;
        this.configParams = configParams;
    }

    public NsiLocalGitConfigManagerImpl(File configPath, NsiMetaDictReader reader, NsiMetaDictWriter writer, NsiConfigParams configParams) {
        this.configPath = configPath;
        this.reader = reader;
        this.writer = writer;
        this.configParams = configParams;
    }

    @Override
    public NsiConfig getConfig() {
        if(config == null) {
            config = readConfig();
        }
        return config;
    }

    public NsiConfigImpl readConfig() {
        NsiConfigImpl config = new NsiConfigImpl(configParams);
        Set<File> configFiles = findFiles();
        for (File configFile : configFiles) {
            MetaDict metaDict = readConfigFile(configFile);
            config.addDict(metaDict);
        }
        config.postCheck();
        return config;
    }


    public Set<File> findFiles() {
        FileFilter fileFilter =
            FileFilterUtils.or(
                    FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE),
                    FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".yaml")));

        ConfigFileWalker walker = new ConfigFileWalker(fileFilter);
        return walker.find(configPath);
    }

    public MetaDict readConfigFile(File configFile) {
        try(InputStream stream = new FileInputStream(configFile)) {
            return reader.read(stream);
        } catch(Exception e) {
            throw new NsiConfigException("read config file: " + configFile.toString(),e);
        }
    }

    public void writeConfigFile (MetaDict metaDict)  {
        File newFile = new File(configPath,metaDict.getName().concat(".yaml"));

        try (FileWriter newFileWriter = new FileWriter(newFile)) {
            writer.write(metaDict,newFileWriter);
            checkConfigFile(newFile);
        } catch (Exception e) {
            log.error("writeConfigFile [{}] -> error", metaDict.getName().concat(".yaml"), e);
        }

        config.updateDict(metaDict);
        config.postCheck();

    }

    private void checkConfigFile(File configFile) {
        try {
            this.readConfigFile(configFile);
        } catch (NsiConfigException e) {
            configFile.delete();
            throw e;
        }
    }

}
