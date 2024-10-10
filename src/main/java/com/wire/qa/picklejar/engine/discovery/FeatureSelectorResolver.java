package com.wire.qa.picklejar.engine.discovery;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.wire.qa.picklejar.engine.PicklejarConfiguration;
import com.wire.qa.picklejar.engine.annotations.AnnotationPattern;
import com.wire.qa.picklejar.engine.descriptor.FeatureDescriptor;
import com.wire.qa.picklejar.engine.descriptor.ScenarioDescriptor;
import com.wire.qa.picklejar.engine.exception.DiscoveryException;
import com.wire.qa.picklejar.engine.exception.ExceptionHelper;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Tag;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import static org.junit.platform.commons.util.StringUtils.isNotBlank;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

public class FeatureSelectorResolver implements SelectorResolver {

    private static final Logger logger = Logger.getLogger(FeatureSelectorResolver.class.getName());
    private static final Parser<Feature> PARSER = new Parser<>(new AstBuilder());

    protected final UniqueId uniqueId;
    protected final PicklejarConfiguration configuration;
    protected final Map<AnnotationPattern, Method> methodCache;
    protected final FeatureFileFilter fileFilter = new FeatureFileFilter();

    FeatureSelectorResolver(UniqueId uniqueId, PicklejarConfiguration configuration, Map<AnnotationPattern, Method> methodCache) {
        this.uniqueId = uniqueId;
        this.configuration = configuration;
        this.methodCache = methodCache;
    }

    @Override
    public Resolution resolve(PackageSelector selector, Context context) {
        String packageName = selector.getPackageName();
        // packageName is not set when running via IntelliJ JUnit Run configuration
        if (StringUtils.isBlank(packageName)) {
            packageName = configuration.getFeaturesPackageName();
        }
        Preconditions.condition(
                isNotBlank(packageName),
                "com.wire.qa.picklejar.features.package must not be null or blank");

        String finalPackageName = packageName;
        logger.info(() -> String.format("[Discovery] Features: Searching package %s", finalPackageName));

        Collection<File> featureFiles = ResourceSeeker.scanForFilesInPackage(packageName, fileFilter);

        Preconditions.condition(featureFiles.size() > 0,
                String.format("Could not find files with extension '.%s' in any provided package: %s",
                        FeatureFileFilter.EXTENSION,
                        packageName));

        logger.info(() -> String.format("[Discovery] Features: Found %s feature(s)", featureFiles.size()));

        Set<Match> matches = new HashSet<>();

        for (File featureFile : featureFiles) {
            matches.addAll(discoverScenarios(featureFile, context));
        }
        return matches.isEmpty() ? unresolved() : Resolution.matches(matches);
    }

    @Override
    public Resolution resolve(FileSelector selector, Context context) {
        return Resolution.matches(discoverScenarios(selector.getFile(), context));
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        String packageName = configuration.getFeaturesPackageName();
        logger.info(() -> String.format("[Discovery] Features: Searching package %s", packageName));

        Collection<File> featureFiles = ResourceSeeker.scanForFilesInPackage(packageName, fileFilter);

        Preconditions.condition(featureFiles.size() > 0,
                String.format("Could not find files with extension '.%s' in any provided package: %s",
                        FeatureFileFilter.EXTENSION,
                        packageName));

        logger.info(() -> String.format("[Discovery] Features: Found %s feature(s)", featureFiles.size()));

        Set<Match> scenarios = new HashSet<>();

        for (File featureFile : featureFiles) {
            scenarios.addAll(discoverScenarios(featureFile, context));
        }
        logger.info(() -> "[Discovery] Features: Filter by UniqueId: " + selector.getUniqueId());
        // Recursively go through children
        Set<TestDescriptor> descriptors = scenarios.stream().map(Match::getTestDescriptor).collect(Collectors.toSet());

        Optional<? extends TestDescriptor> match = descriptors.stream().findFirst().get().findByUniqueId(selector.getUniqueId());
        Set<Match> matches = new HashSet<>();
        match.ifPresent(i -> matches.add(Match.exact(i)));

        return matches.isEmpty() ? unresolved() : Resolution.matches(matches);
    }

    private Set<Match> discoverScenarios(File file, Context context) {
        // TODO: Maybe return new scenario selectors instead of matches?
        logger.fine(() -> String.format("[Discovery] Scenarios: Searching scenarios in file %s", file.getAbsolutePath()));

        // Check for file existence and correct file extension
        Preconditions.condition(file.exists(), String.format("Cannot find feature file: %s", file.getAbsolutePath()));
        Preconditions.condition(fileFilter.accept(null, file.getName()),
                String.format("Supplied feature file does not end with extension '.%s': %s",
                        FeatureFileFilter.EXTENSION,
                        file.getAbsolutePath()));

        Feature feature = null;

        try {
            feature = PARSER.parse(readFile(file));
        } catch (IOException e) {
            throw new PreconditionViolationException(
                    String.format("Could not read file %s: %s", file.getName(), e.getMessage()));
        }

        // Check feature naming
        Preconditions.notNull(feature, "");
        Preconditions.notNull(feature.getName(), String.format("Feature is missing name in file: %s", file.getAbsolutePath()));
        feature = normalizeName(feature);

        return discoverScenarios(feature, file, context);
    }

    private Set<Match> discoverScenarios(Feature feature, File file, Context context) {
        ScenarioSelectorResolver scenarioSelectorResolver = new ScenarioSelectorResolver(uniqueId, configuration, methodCache);
        Set<Match> matches = new HashSet<>();
        String relativeFolder = getRelativeFeatureFolder(file, configuration.getFeaturesPackagePaths());
        FeatureDescriptor featureDescriptor = new FeatureDescriptor(uniqueId, feature.getName(), file, relativeFolder);
        List<String> tags = new ArrayList<>();
        for (Tag tag : feature.getTags()) {
            tags.add(tag.getName());
        }
        featureDescriptor.setTags(tags);

        List<ScenarioDefinition> filteredScenarios = feature.getScenarioDefinitions();

        List<ScenarioDescriptor> scenarioDescriptors = new ArrayList<>();
        for (ScenarioDefinition scenarioDefinition : filteredScenarios) {
            scenarioDescriptors.addAll(scenarioSelectorResolver.discover(scenarioDefinition,
                    featureDescriptor));
        }

        logger.fine(() -> "[Discovery] Check for duplicate scenarios");
        List<String> names = scenarioDescriptors.stream().map(TestDescriptor::getDisplayName).collect(Collectors.toList());
        Set<String> items = new HashSet<>();
        Set<String> duplicates = names.stream().filter(n -> !items.add(n)).collect(Collectors.toSet());
        if (duplicates.size() > 0) {
            throw new DiscoveryException(
                    String.format("Duplicate scenario name(s) in feature '%s': %s",
                            feature.getName(),
                            String.join(",", duplicates)),
                    featureDescriptor.getFile(),
                    ExceptionHelper.getLineNumberInFeature(featureDescriptor.getFile(),
                            duplicates.iterator().next().replaceAll(" [0-9]+$", "")));
        }

        for (ScenarioDescriptor scenarioDescriptor : scenarioDescriptors) {
            featureDescriptor.addChild(scenarioDescriptor);
        }

        logger.info(String.format("[Discovery] Scenarios: Found %s scenario(s) in feature \"%s%s\"",
                featureDescriptor.getChildren().size(),
                relativeFolder,
                feature.getName()));

        context.addToParent(parent -> Optional.of(featureDescriptor));
        matches.add(Match.exact(featureDescriptor));
        return matches;
    }

    private static String readFile(File file) throws IOException {
        logger.fine(() -> String.format("[Discovery] Scenarios: Reading file: %s", file.getName()));
        if (!file.isFile()) {
            throw new IllegalArgumentException(String.format("Provided file is a folder: %s",
                    file.getAbsolutePath()));
        }
        return Files.readString(Paths.get(file.getAbsolutePath()));
    }

    public static Feature normalizeName(Feature feature) {
        String name = feature.getName()
                .replaceAll("[^a-zA-Z0-9 ]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return new Feature(feature.getTags(), feature.getLocation(), feature.getLanguage(), feature.getKeyword(),
                name, feature.getDescription(), feature.getBackground(), feature.getScenarioDefinitions(),
                feature.getComments());
    }

    // Only public because of tests
    public static String getRelativeFeatureFolder(File file, Set<File> featuresPackageFolders) {
        logger.info("file: " + file.toURI());
        featuresPackageFolders.forEach(folder -> logger.info("folder: " + folder.toURI()));
        Optional<String> relativeFolder = featuresPackageFolders.stream()
                .map(folder -> folder.toURI().relativize(file.getParentFile().toURI()).getPath())
                .filter(result -> !result.startsWith(File.separator))
                .findFirst();
        if (relativeFolder.isEmpty()) {
            throw new RuntimeException(String.format(
                    "[Discovery] Could not relate any feature package folders to file %s",
                    file.getAbsolutePath()));
        } else {
            return relativeFolder.get();
        }
    }
}
