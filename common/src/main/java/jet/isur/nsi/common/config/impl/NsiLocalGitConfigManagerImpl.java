package jet.isur.nsi.common.config.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jet.isur.nsi.api.NsiConfigManager;
import jet.isur.nsi.api.NsiMetaDictReader;
import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigParams;
import jet.isur.nsi.api.model.MetaDict;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;

public class NsiLocalGitConfigManagerImpl implements NsiConfigManager {

    private final class ConfigFileWalker extends DirectoryWalker<File> {
        private ConfigFileWalker(FileFilter filter) {
            super(filter, -1);
        }

        @Override
        protected void handleFile(File file, int depth, Collection<File> results)
                throws IOException {
            results.add(file);
        }

        public Set<File> find(File configPath) {
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
    private final NsiConfigParams configParams;
    private NsiConfigImpl config;

    public NsiLocalGitConfigManagerImpl(File configPath, NsiMetaDictReader reader, NsiConfigParams configParams) {
        this.configPath = configPath;
        this.reader = reader;
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
}
