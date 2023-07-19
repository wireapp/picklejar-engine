package com.wire.qa.picklejar.engine.discovery;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.wire.qa.picklejar.engine.PicklejarConfiguration;
import com.wire.qa.picklejar.engine.annotations.AnnotationPattern;
import com.wire.qa.picklejar.engine.descriptor.FeatureDescriptor;
import com.wire.qa.picklejar.engine.descriptor.MethodDescriptor;
import com.wire.qa.picklejar.engine.descriptor.ScenarioDescriptor;
import com.wire.qa.picklejar.engine.descriptor.StepDescriptor;
import com.wire.qa.picklejar.engine.exception.DiscoveryException;
import com.wire.qa.picklejar.engine.exception.ExceptionHelper;
import com.wire.qa.picklejar.engine.exception.MethodForStepNotFoundException;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.discovery.SelectorResolver;

public class ScenarioSelectorResolver implements SelectorResolver {

    private static final Logger logger = Logger.getLogger(ScenarioSelectorResolver.class.getName());

    protected final UniqueId uniqueId;
    protected final PicklejarConfiguration configuration;
    protected final Map<AnnotationPattern, Method> methodCache;
    // Following characters are forbidden because they are used to structure the -Dtest parameter
    protected final String[] forbiddenCharacters = {"+", "#", "\\", "\""};

    ScenarioSelectorResolver(UniqueId uniqueId, PicklejarConfiguration configuration, Map<AnnotationPattern, Method> methodCache) {
        this.uniqueId = uniqueId;
        this.configuration = configuration;
        this.methodCache = methodCache;
    }

    public List<ScenarioDescriptor> discover(ScenarioDefinition scenarioDefinition, FeatureDescriptor featureDescriptor) {
        List<ScenarioDescriptor> scenarioDescriptors = new ArrayList<>();

        // Check scenario naming
        Preconditions.notNull(scenarioDefinition, "");
        Preconditions.notNull(scenarioDefinition.getName(),
                String.format("Scenario is missing name in feature %s", featureDescriptor.getFeature().getName()));
        for (String illegalCharacter : forbiddenCharacters) {
            if (scenarioDefinition.getName().contains(illegalCharacter)) {
                String message = String.format("[Discovery] Scenario '%s' contains illegal character '%s' in name in feature '%s'",
                        scenarioDefinition.getName(),
                        illegalCharacter,
                        featureDescriptor.getFeature().getName()
                );
                throw new DiscoveryException(message, featureDescriptor.getFile(),
                        ExceptionHelper.getLineNumberInFeature(featureDescriptor.getFile(), scenarioDefinition.getName()));
            }
        }

        List<String> tags = new ArrayList<>();
        List<Step> steps = scenarioDefinition.getSteps();

        for (TestTag tag : featureDescriptor.getTags()) {
            tags.add(tag.getName());
        }

        for (Tag tag : scenarioDefinition.getTags()) {
            tags.add(tag.getName());
        }

        if (scenarioDefinition instanceof ScenarioOutline) {
            logger.finest(() -> "[Discovery] Discovered Scenario Outline: " + scenarioDefinition.getName());
            ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;

            for (int i = 0; i < scenarioOutline.getExamples().size(); i++) {
                TableRow tableHeader = scenarioOutline.getExamples().get(i).getTableHeader();
                List<TableRow> tableRows = scenarioOutline.getExamples().get(i).getTableBody();

                for (int j = 0; j < tableRows.size(); j++) {
                    TableRow tableRow = tableRows.get(j);
                    Map<String, String> exampleRowWithHeader = new HashMap<>();
                    for (int k = 0; k < tableRow.getCells().size(); k++) {
                        String key = tableHeader.getCells().get(k).getValue();
                        String value = tableRow.getCells().get(k).getValue();
                        exampleRowWithHeader.put(key, value);
                    }
                    logger.finest(() -> "[Discovery] Scenarios: Add scenario: " + scenarioDefinition.getName());
                    ScenarioDescriptor scenarioDescriptor = new ScenarioDescriptor(
                            featureDescriptor.getUniqueId(),
                            replaceExampleOccurences(scenarioDefinition.getName(), exampleRowWithHeader),
                            j,
                            featureDescriptor.getFile());
                    scenarioDescriptor.setTags(tags);
                    scenarioDescriptor.setStepDescriptors(discoverSteps(scenarioDescriptor, steps, exampleRowWithHeader));
                    scenarioDescriptors.add(scenarioDescriptor);
                    logger.fine(() -> "[Discovery] Scenarios: Added: " + scenarioDescriptor.getUniqueId());
                }
            }
        } else {
            logger.fine(() -> String.format("[Discovery] Scenarios: Found Scenario \"%s\"", scenarioDefinition.getName()));
            ScenarioDescriptor scenarioDescriptor = new ScenarioDescriptor(
                    featureDescriptor.getUniqueId(),
                    scenarioDefinition.getName(),
                    featureDescriptor.getFile());
            scenarioDescriptor.setTags(tags);
            scenarioDescriptor.setStepDescriptors(discoverSteps(scenarioDescriptor, steps, new HashMap<>()));
            scenarioDescriptors.add(scenarioDescriptor);
        }
        return scenarioDescriptors;
    }

    private List<StepDescriptor> discoverSteps(ScenarioDescriptor scenario, List<Step> steps, Map<String, String> exampleValues) {
        List<StepDescriptor> stepDescriptors = new ArrayList<>();

        // search text in annotation
        for (Step step : steps) {
            StepDescriptor stepDescriptor = new StepDescriptor();
            stepDescriptor.setKeyword(step.getKeyword());
            stepDescriptor.setDisplayName(replaceExampleOccurences(step.getText(), exampleValues));
            stepDescriptor.setMethodDescriptor(discoverMethodDescriptor(step.getText(), exampleValues, scenario.getFile()));
            stepDescriptor.setParent(scenario);
            stepDescriptors.add(stepDescriptor);
        }
        return stepDescriptors;
    }

    private MethodDescriptor discoverMethodDescriptor(String rawText, Map<String, String> exampleParams, File file) {
        final String text = replaceExampleOccurences(rawText, exampleParams);
        List<MethodDescriptor> results = new ArrayList<>();

        logger.finest(() -> "[Discovery] Methods: Discover methods");

        for (Map.Entry<AnnotationPattern, Method> entrySet : methodCache.entrySet()) {
            final AnnotationPattern pattern = entrySet.getKey();
            final Method method = entrySet.getValue();

            logger.finest(() -> "[Discovery] Methods: Check method: " + method.getName());

            final Matcher matcher = pattern.getPattern().matcher(text);

            logger.finest(() -> "[Discovery] Methods: Check regex: " + pattern.getAnnotation());

            if (matcher.matches()) {
                logger.finest(() -> String.format("[Discovery] Method %s matches", method.getName()));

                if (matcher.groupCount() == method.getParameterTypes().length) {
                    logger.finest(() -> "[Discovery] Number of Regex groups and number of method paramaters match");
                } else {
                    logger.warning(String.format("[Discovery] Number of Regex groups and number of method paramaters do not match:\n"
                                    + "Regex: %s\n"
                                    + "Method: %s.%s()",
                            pattern.getAnnotation(),
                            method.getDeclaringClass().getName(),
                            method.getName()));
                    logger.finest(() -> "[Discovery] Parameters do not match - Looking for other method");
                    continue;
                }

                final List<Object> params = new ArrayList<>();

                Class<?>[] types = method.getParameterTypes();
                logger.finest(() -> String.format("[Discovery] Methods: Expected parameter types: \n%s", new Object[]{Arrays.asList(types)}));
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    params.add(tryCast(matcher.group(i), types[i - 1], rawText, file));
                }
                logger.finest(() -> String.format("[Discovery] Methods: Actual parameters: \n%s", new Object[]{params}));
                logger.finest(() -> String.format("[Discovery] Methods: Found method %s with %s", method.getName(), params));

                results.add(new MethodDescriptor(method, params.toArray()));
                if (!configuration.doMultipleStepsMatchingWarning()) {
                    break;
                }
            }
        }
        if (results.size() < 1) {
            String message = String.format("Could not find match for step '%s'", rawText);
            throw new MethodForStepNotFoundException(message, file,
                    ExceptionHelper.getLineNumberInFeature(file, rawText));
        } else if (results.size() > 1) {
            List<String> methodList = results.stream().map(m -> m.getMethod().getName()).collect(Collectors.toList());
            logger.warning(() -> String.format("Multiple matches for step '%s': %s", rawText, String.join(",", methodList)));
        }
        return results.get(0);
    }

    public String replaceExampleOccurences(String rawStep, Map<String, String> exampleParams) {
        for (String key : exampleParams.keySet()) {
            rawStep = rawStep.replaceAll(Matcher.quoteReplacement("<" + key + ">"), Matcher.quoteReplacement(exampleParams.get(key)));
        }
        return rawStep;
    }

    private static Object tryCast(String thingToCast, Class<?> clazz, String step, File file) {
        switch (clazz.toString()) {
            case "short":
            case "class java.lang.Short":
                try {
                    return Integer.parseInt(thingToCast);
                } catch (NumberFormatException e) {
                    break;
                }
            case "int":
            case "class java.lang.Integer":
                try {
                    if (thingToCast == null) return 0;
                    return Integer.parseInt(thingToCast);
                } catch (NumberFormatException e) {
                    break;
                }
            case "long":
            case "class java.lang.Long":
                try {
                    return Long.parseLong(thingToCast);
                } catch (NumberFormatException e) {
                    break;
                }
            case "float":
            case "class java.lang.Float":
                try {
                    return Float.parseFloat(thingToCast);
                } catch (NumberFormatException e) {
                    break;
                }
            case "double":
            case "class java.lang.Double":
                try {
                    return Double.parseDouble(thingToCast);
                } catch (NumberFormatException e) {
                    break;
                }
            case "char":
            case "class java.lang.Character":
                try {
                    return thingToCast.charAt(0);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            case "byte":
            case "class java.lang.Byte":
                try {
                    return Byte.parseByte(thingToCast);
                } catch (NumberFormatException e) {
                    break;
                }
            case "boolean":
            case "class java.lang.Boolean":
                return Boolean.parseBoolean(thingToCast);
            case "class java.lang.String":
                return thingToCast;
        }
        String message = String.format("[Discovery] Methods: Step '%s' has wrong parameter type. Expected type: %s. Actual value: %s", step, clazz.getName(), thingToCast);
        throw new MethodForStepNotFoundException(message, file,
                ExceptionHelper.getLineNumberInFeature(file, step));
    }
}
