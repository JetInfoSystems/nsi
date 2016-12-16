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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
    
    private static final String TMP_DIR = ".tmp";
    private static final String FILE_EXTENTION = ".yaml";

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
            throw new NsiConfigException("checkoutNewConfig  from  " + from + " failed", e);
        }

    }

    public NsiConfigImpl readConfig() {
        NsiConfigImpl config = new NsiConfigImpl(configParams);
        Set<File> configFiles = findFiles();
        for (File configFile : configFiles) {
            MetaDict metaDict = readConfigFile(configFile);
            config.addDict(metaDict);
            config.savePath(metaDict.getName(), configFile.toPath());
        }
        config.postCheck();
        return config;
    }

    public Set<File> findFiles() {
        FileFilter fileFilter = 
            FileFilterUtils.or(
                FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE, FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(TMP_DIR))),
                FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".yaml"))
                );

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
        createOrUpdateConfig(metaDict, "");
    }
    
    public void createOrUpdateConfig(MetaDict metaDict, String relativePath) {
        // Сохраняем текущий путь до файла и текущую версию справочника, 
        // чтобы удалить после успешного сохранения и если путь до файла изменится
        Path curPath = config.getMetaDictPath(metaDict.getName());
        MetaDict curMetaDict = config.getMetaDict(metaDict.getName());
        
        
        Path tmpPath = getTmpPath();
        Path metaDictTempFilePath = getCreatePathAndFile(tmpPath, metaDict.getName());
        
        checkAndWriteTempFile(metaDict, metaDictTempFilePath, curMetaDict, curPath);
        
        Path targetPath = configPath.toPath().resolve(relativePath).resolve(getDictFileName(metaDict.getName()));
        moveTempFileToTargetPath(metaDict.getName(), curPath, targetPath, metaDictTempFilePath);
        
        log.info("createOrUpdateConfig [{}] -> ok", metaDict.getName());
    }
    
    private void moveTempFileToTargetPath(String name, Path curPath, Path targetPath, Path tempFilePath) {
        try {
            //Проверяем наличие директорий
            checkCreateDirs(targetPath);
            // перемещаем временный файл в целевой каталог
            Files.move(tempFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            config.savePath(name, targetPath);
            
            if (curPath != null && !targetPath.equals(curPath)) {
                // если предыдущая версия файла существовала файла по другому пути, то удаляем старый файл
                Files.deleteIfExists(curPath);
            }
            
            log.debug("moveTempFileToTargetPath [{}] -> ok", name);
        } catch (IOException e) {
            log.error("moveTempFileToTargetPath ['{}', '{}', '{}', '{}'] -> failed", name, curPath, targetPath, tempFilePath);
            throw new NsiConfigException("Failed to move new version of metaDict '" + name + "' to target path '" + targetPath + "'", e);
        }
    } 
    
    private void checkAndWriteTempFile(MetaDict metaDict, Path metaDictFilePath, MetaDict oldMetaDict, Path oldMetaDictFilePath) {
        try(FileWriter newFileWriter = new FileWriter(metaDictFilePath.toFile())) {
            // Записываем промежуточную копию на диск
            writer.write(metaDict, newFileWriter);

            // Добавляем/обновляем метаданные в памяти, также выполняется проверка при добавлении
            config.updateDict(metaDict);
            config.postCheck();
            
            // Читаем записанное в файл для проверки корректности записи
            readConfigFile(metaDictFilePath.toFile());
            log.debug("checkAndWriteTempFile [{}] -> ok", metaDict.getName());
        } catch(Exception e) {
            // В случае любых проблем, удаляем из памяти, 
            // но файл во временной директории можем оставить для разбора проблемы
            log.error("checkAndWriteTempFile [{}] -> error", metaDict.getName(), e);
            // Удалим из памяти, если успели добавить
            config.removeDict(metaDict.getName());
            // если предыдущая версия существовала, вернем в память
            if(oldMetaDict != null) {
                config.addDict(oldMetaDict);
                config.savePath(oldMetaDict.getName(), oldMetaDictFilePath);
            }

            throw new NsiConfigException("Failed to add/update config file for metaDict: " + metaDict.getName(), e);
        }
    }
    
    private Path getCreatePathAndFile(Path dir, String dictName) {
        Path metaDictFilePath = Paths.get(dir.toString(), getDictFileName(dictName));
        if (Files.notExists(metaDictFilePath)) {
            try {
                Files.createFile(metaDictFilePath);
                log.debug("getCreatePathAndFile['{}'] -> new file created");
            } catch (IOException ex) {
                log.error("getCreatePathAndFile['{}'] -> failed to create file in directory ['{}', '{}']", 
                                        dictName, metaDictFilePath.toString(), dir.toString(), ex);
                throw new NsiConfigException("couldn't create file " + metaDictFilePath.toString() + 
                                        " in directory '" + dir.toString() + "'", ex);
            }
        }
        return metaDictFilePath;
    }
    
    private void checkCreateDirs(Path targetPath) {
        if (Files.notExists(targetPath)) {
            try {
                Files.createDirectories(targetPath);
            } catch (IOException ex) {
                log.error("checkCreateDirs -> failed to create directory ['{}']", targetPath.toString(), ex);
                throw new NsiConfigException("couldn't create directory " + targetPath.toString(), ex);
            }
        }
    }
    
    private Path getTmpPath() {
        Path tmpPath = Paths.get(configPath.toPath().toString(), TMP_DIR);
        checkCreateDirs(tmpPath);
        return tmpPath;
    }
    
    private String getDictFileName(String dictName) {
        return dictName.concat(FILE_EXTENTION);
    }
    
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


}
