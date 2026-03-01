package io.github.jonasrutishauser.liberty.features;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openliberty.tools.common.plugins.util.PluginExecutionException;
import io.openliberty.tools.common.plugins.util.PluginScenarioException;
import io.openliberty.tools.common.plugins.util.PrepareFeatureUtil;

public class FeaturesJsonGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeaturesJsonGenerator.class);

    public static void main(String[] args) throws PluginScenarioException, PluginExecutionException, IOException {
        PrepareFeatureUtil util = new PrepareFeatureUtil(new File(System.getProperty("liberty.dir", "target/liberty/wlp")), System.getProperty("openliberty.version", "26.0.0.1")) {
            
            @Override
            public void warn(String msg) {
                LOGGER.warn(msg);
            }
            
            @Override
            public boolean isDebugEnabled() {
                return LOGGER.isDebugEnabled();
            }
            
            @Override
            public void info(String msg) {
                LOGGER.info(msg);
            }
            
            @Override
            public void error(String msg, Throwable e) {
                LOGGER.error(msg, e);
            }
            
            @Override
            public void error(String msg) {
                LOGGER.error(msg);
            }
            
            @Override
            public File downloadArtifact(String groupId, String artifactId, String type, String version)
                    throws PluginExecutionException {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public void debug(String msg, Throwable e) {
                LOGGER.debug(msg, e);
            }
            
            @Override
            public void debug(Throwable e) {
                LOGGER.debug("", e);
            }
            
            @Override
            public void debug(String msg) {
                LOGGER.debug(msg);
            }
        };
        
        Map<File, String> esaFileMap = new HashMap<>();
        Path basedir = Path.of((String) args[0]);
        Files.walkFileTree(basedir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".esa")) {
                    esaFileMap.put(file.toFile(), StreamSupport
                            .stream(file.subpath(basedir.getNameCount(), file.getNameCount() - 3).spliterator(), false)
                            .map(Path::toString).collect(Collectors.joining(".")));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        LOGGER.debug("esaFileMap: {}", esaFileMap);
        util.generateJson(System.getProperty("target.file", "target/features.json"), esaFileMap);
    }

}