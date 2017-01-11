package jet.nsi.common.config.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
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
        protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
            results.add(file);
        }

        Set<File> find(File configPath) {
            Set<File> result = new HashSet<>();
            try {
                walk(configPath, result);
                return result;
            } catch (Exception e) {
                throw new NsiConfigException("find config files", e);
            }
        }
    }
    
    private final class CopyFileVisitor extends SimpleFileVisitor<Path> {
        private final Path targetPath;
        private Path sourcePath = null;
        
        public CopyFileVisitor(Path targetPath) {
            this.targetPath = targetPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            if (sourcePath == null) {
                sourcePath = dir;
            } else {
            Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    }

    private final File configPath;
    private final NsiMetaDictReader reader;
    private final NsiMetaDictWriter writer;
    private final NsiConfigParams configParams;
    private NsiConfigImpl config;

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

    @Override
    public synchronized NsiConfig reloadConfig() {
        NsiConfigImpl newConfig = null;
        try {
            newConfig = readConfig();
            config = newConfig;
            log.info("reloadConfig -> ok");
            return config;
        } catch (Exception e) {
            log.warn("reloadConfig -> failed", e);
            throw new NsiConfigException("reload config files", e);
        }

    }
    
    // TODO: перейти в дальнейшем на использование git
    @Override
    public synchronized void checkoutNewConfig(String from) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(from), "from must be not empty");
        try {
            Path source = Paths.get(from);
            Files.walkFileTree(source, new CopyFileVisitor(configPath.toPath()));
            log.info("checkoutNewConfig [{}] -> ok", from);
        } catch (IOException e) {
            log.error("checkoutNewConfig [{}] -> failed", from, e);
            throw new NsiConfigException("checkoutNewConfig from  " + from, e);
        }

    }

    public NsiConfigImpl readConfig() {
        NsiConfigImpl config = new NsiConfigImpl(configParams);
        Set<File> configFiles = findFiles();
        for (File configFile : configFiles) {
            log.info("readConfig->reading file {}", configFile);
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
            throw new NsiConfigException("read config file: " + configFile.toString(), e);
        }
    }

    public void createOrUpdateConfig(MetaDict metaDict) {
        File newFile = new File(configPath, metaDict.getName().concat(".yaml"));

        try (OutputStreamWriter newFileWriter = new OutputStreamWriter(new FileOutputStream(newFile),
                StandardCharsets.UTF_8)) {
            config.updateDict(metaDict);
            config.postCheck();

            writer.write(metaDict, newFileWriter);

            log.info("createOrUpdateConfig [{}] -> ok", metaDict.getName());
            readConfigFile(newFile);
        } catch (Exception e) {
            log.error("createOrUpdateConfig [{}] -> error", metaDict.getName(), e);
            // Удалим из памяти, если успели добавить
            config.removeDict(metaDict.getName());

            // Удалим файл, если он существует
            try {
                newFile.delete();
            } catch (Exception ex) {
                log.warn("writeConfigFile[{}] tried to delete file -> file wasn't created", metaDict.getName(), ex);
            }
            throw new NsiConfigException("Failed to write config file for metaDict: " + metaDict.getName(), e);
        }
    }

}
