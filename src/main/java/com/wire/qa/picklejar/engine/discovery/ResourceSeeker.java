package com.wire.qa.picklejar.engine.discovery;

import org.junit.platform.commons.util.Preconditions;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.platform.commons.util.StringUtils.isNotBlank;

public class ResourceSeeker {

    private static final Logger logger = Logger.getLogger(ResourceSeeker.class.getName());

    public static Collection<File> scanForFilesInPackage(String packageName, FilenameFilter fileFilter) {
        Set<File> resourcesDirectories = getResourceDirectoriesFromPackage(packageName);
        return resourcesDirectories.stream().flatMap(
                directory -> {
                    logger.fine(() -> "[Discovery] Features: Searching in directory: " + directory.getAbsolutePath());
                    Preconditions.condition(directory.isDirectory(),
                            "Resource directory cannot be found through feature package name");
                    List<File> files = new ArrayList<>();
                    try (Stream<Path> paths = Files.walk(directory.toPath())) {
                        files = paths.filter(Files::isRegularFile)
                                .map(Path::toFile)
                                .filter(file -> fileFilter.accept(file, file.getName()))
                                .collect(Collectors.toList());
                    } catch (IOException e) {
                        throw new RuntimeException("Error occured when searching for feature files", e);
                    }
                    return files.stream();
                }
        ).collect(Collectors.toList());
    }

    public static Set<File> getResourceDirectoriesFromPackage(String packageName) {
        try {
            Preconditions.condition(
                    isNotBlank(packageName),
                    "basePackageName must not be null or blank");
            packageName = packageName.trim();

            String path = packageName.replace(".", File.separator);
            List<URL> resourceUrl = Collections.list(Thread.currentThread().getContextClassLoader().getResources(path));
            Preconditions.condition(resourceUrl != null,
                    "Resource directory cannot be found through feature package name");
            return resourceUrl.stream().map(url -> {
                try {
                    return Paths.get(url.toURI()).toFile();
                } catch (URISyntaxException e) {
                    logger.warning(() -> String.format("[Discovery] Features: Failed to convert %s to file path", url));
                    return null;
                }
            }).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Error occured when searching for feature files", e);
        }
    }
}
