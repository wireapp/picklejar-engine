package com.wire.qa.picklejar.engine.discovery;

import com.wire.qa.picklejar.engine.annotations.AnnotationPattern;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;

public class MethodCache {

    private static final Logger logger = Logger.getLogger(ScenarioSelectorResolver.class.getName());

    private static final Predicate<Class<?>> allTypes = type -> true;
    private static final Predicate<String> allNames = name -> true;
    private static final Predicate<String> stepFiles = name -> name.matches("^.*Steps?$");
    private static final Predicate<Method> annotatedMethods = method -> method.getAnnotations().length > 0;
    private static final String ANNOTATION_VALUE_METHOD_NAME = "value";
    private static final String ANNOTATIONS_PACKAGE_NAME = "io.cucumber.java.en";

    private Map<AnnotationPattern, Method> methodCache = new HashMap<>();

    public MethodCache(List<String> stepsPackages) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        generateMethodCache(stepsPackages);
    }

    public Map<AnnotationPattern, Method> getCache() {
        return methodCache;
    }

    private Map<AnnotationPattern, Method> generateMethodCache(List<String> stepsPackages) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for (String basePackageName : stepsPackages) {
            logger.info("[Discovery] Steps: Searching package " + basePackageName);
            Collection<Class<?>> annotationClasses = ReflectionUtils.findAllClassesInPackage(ANNOTATIONS_PACKAGE_NAME, Class::isAnnotation, allNames);
            Collection<Class<?>> loadedClasses = ReflectionUtils.findAllClassesInPackage(basePackageName, allTypes, stepFiles);
            logger.finest(() -> String.format("[Discovery] Steps: Loaded %d class(es)", loadedClasses.size()));
            for (Class<?> loadedClass : loadedClasses) {
                logger.finest(() -> "[Discovery] Steps: Loaded class: " + loadedClass.getName());

                for (Method method : ReflectionUtils.findMethods(loadedClass, annotatedMethods, TOP_DOWN)) {
                    List<Annotation> matchingAnnotations = Arrays
                            .stream(method.getAnnotations())
                            .filter(a -> annotationClasses.contains(a.annotationType()))
                            .collect(Collectors.toList());
                    if (matchingAnnotations.size() != 1) {
                        logger.info(() ->
                                String.format("[Discovery] Method %s in class %s contains multiple or no Gherkin annotations: %s",
                                        method.getName(),
                                        loadedClass.getName(),
                                        matchingAnnotations
                                                .stream()
                                                .map(a -> a.annotationType().getName())
                                                .collect(Collectors.joining(","))));
                    } else {
                        logger.finest(() -> "[Discovery] Steps: Make annotation value accessible");
                        final Method annotationValueMethod =
                                matchingAnnotations.get(0).getClass().getMethod(ANNOTATION_VALUE_METHOD_NAME);
                        annotationValueMethod.setAccessible(true);
                        final String annotationValue = (String) annotationValueMethod.invoke(matchingAnnotations.get(0));
                        logger.finest(() -> String.format("[Discovery] Steps: Found method \"%s\" with regex \"%s\"",
                                method.getName(),
                                annotationValue));
                        AnnotationPattern pattern = new AnnotationPattern(annotationValue);
                        if (methodCache.containsKey(pattern)) {
                            logger.warning(() -> String.format("[Discovery] Duplicated regex '%s' found! Method %s.%s() vs. %s.%s()",
                                    pattern.getAnnotation(),
                                    loadedClass.getName(),
                                    method.getName(),
                                    methodCache.get(pattern).getDeclaringClass().getName(),
                                    methodCache.get(pattern).getName()
                            ));
                        } else {
                            methodCache.put(pattern, method);
                        }
                    }
                }
            }
        }
        logger.info(String.format("[Discovery] Steps: Add %d step method(s) into MethodCache", methodCache.size()));

        return methodCache;
    }

}
