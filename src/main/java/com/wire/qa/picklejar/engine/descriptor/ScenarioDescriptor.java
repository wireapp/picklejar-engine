package com.wire.qa.picklejar.engine.descriptor;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.wire.qa.picklejar.engine.TestContext;
import com.wire.qa.picklejar.engine.PicklejarEngineExecutionContext;
import com.wire.qa.picklejar.engine.annotations.AfterEachScenario;
import com.wire.qa.picklejar.engine.annotations.AfterEachStep;
import com.wire.qa.picklejar.engine.annotations.BeforeEachScenario;
import com.wire.qa.picklejar.engine.annotations.BeforeEachStep;
import com.wire.qa.picklejar.engine.exception.SkipException;
import com.wire.qa.picklejar.engine.gherkin.model.Around;
import com.wire.qa.picklejar.engine.gherkin.model.Feature;
import com.wire.qa.picklejar.engine.gherkin.model.Result;
import com.wire.qa.picklejar.engine.gherkin.model.Scenario;
import com.wire.qa.picklejar.engine.gherkin.model.Step;
import com.wire.qa.picklejar.engine.gherkin.model.Tag;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.Node;

import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;

public class ScenarioDescriptor extends AbstractTestDescriptor implements Node<PicklejarEngineExecutionContext> {

    private static final Logger logger = Logger.getLogger(ScenarioDescriptor.class.getName());

    private final Map<String, Object> classInstanceCache = new ConcurrentHashMap<>();
    private TestContext testContext = null;

    private Scenario scenario = null;
    private Set<TestTag> tags = new HashSet<>();
    private File file;
    private List<StepDescriptor> stepDescriptors;

    private TestExecutionResult executionResult;

    public ScenarioDescriptor(UniqueId uniqueId, String scenarioName, File file) {
        // Example: [engine:picklejar-engine]/[Feature:Examples]/[Scenario:Test without examples]
        super(uniqueId.append("Scenario", scenarioName), scenarioName);
        this.file = file;
    }

    public ScenarioDescriptor(UniqueId uniqueId, String scenarioName, int exampleNumber, File file) {
        // Example: [engine:picklejar-engine]/[Feature:Examples]/[Scenario:Test with examples]/[Example:0]
        super(uniqueId
                        .append("Scenario", scenarioName)
                        .append("Example", String.valueOf(exampleNumber)),
                scenarioName + " " + exampleNumber);
        this.file = file;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Set<TestTag> getTags() {
        return tags;
    }

    public List<StepDescriptor> getStepDescriptors() {
        return stepDescriptors;
    }

    public void setStepDescriptors(List<StepDescriptor> stepDescriptors) {
        this.stepDescriptors = stepDescriptors;
    }

    public void setTags(List<String> tags) {
        this.tags = tags.stream().map(TestTag::create).collect(Collectors.toSet());
    }

    public File getFile() {
        return file;
    }

    // Overriding getExclusiveResources() makes it possible to lock resources on runtime
    @Override
    public Set<ExclusiveResource> getExclusiveResources() {
        return getTags().stream()
                .filter(t -> t.getName().startsWith("@resource="))
                .map(t -> new ExclusiveResource(
                        t.getName().replace("@resource=", ""),
                        ExclusiveResource.LockMode.READ_WRITE)
                )
                .collect(Collectors.toSet());
    }

    private Scenario getScenario() {
        if (scenario == null) {
            Feature feature = ((FeatureDescriptor) getParent().get()).getFeature();

            List<Step> steps = new ArrayList<>();
            for (StepDescriptor stepDescriptor : stepDescriptors) {
                steps.add(stepDescriptor.getStep());
            }

            scenario = new Scenario(feature, getDisplayName(), "", 0,
                    "Scenario Outline", steps, getTags().stream().map(t -> new Tag(t.getName())).collect(Collectors.toList()));
        }
        return scenario;
    }

    public ExecutionMode getExecutionMode() {
        return ExecutionMode.CONCURRENT;
    }

    public PicklejarEngineExecutionContext before(PicklejarEngineExecutionContext context) {
        logger.info("=".repeat(77));
        logger.info(() -> String.format("[%d] TAGS: %s",
                Thread.currentThread().getId(),
                this.getScenario().getTags().stream().map(Tag::getName).collect(Collectors.joining(" "))));
        logger.info(() -> String.format("[%d] SCENARIO: \"%s: %s\"",
                Thread.currentThread().getId(),
                this.getScenario().getFeature().getName(),
                this.getScenario().getName()));
        logger.info(("=").repeat(77));
        // execute @BeforeEachScenario
        for (Class<?> c : context.getTestClassesFromFirstStepsPackage()) {
            for (Method method : AnnotationUtils.findAnnotatedMethods(c, BeforeEachScenario.class, TOP_DOWN)) {
                if (method.getReturnType() == TestContext.class) {
                    Instant endTime = null;
                    Instant startTime = Instant.now();
                    try {
                        Preconditions.condition(method.getParameterCount() == 1,
                                "@BeforeEachScenario method has wrong number of parameters");
                        Map<String, String> reportEntries = new HashMap<>();
                        reportEntries.put("class", c.getCanonicalName() + "." + method.getName());
                        reportEntries.put("scenario", this.getScenario().getName());
                        reportEntries.put("status", "started");
                        context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
                        Object returnValue = ReflectionUtils.invokeMethod(method, ReflectionUtils.newInstance(c),
                                getScenario());
                        if (returnValue != null) {
                            Preconditions.condition(returnValue instanceof TestContext,
                                    "@BeforeEachScenario method is missing return value that extends TestContext");
                            this.testContext = (TestContext) returnValue;
                        }
                    } catch (Exception e) {
                        endTime = Instant.now();
                        Result result = new Result(Duration.between(startTime, endTime).toNanos(), Result.FAILED,
                                e.getMessage());
                        this.getScenario().setBefore(new Around(result));
                        logger.severe("[BeforeEachScenario] Exception: " + e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                }
                Map<String, String> reportEntries = new HashMap<>();
                reportEntries.put("class", c.getCanonicalName() + "." + method.getName());
                reportEntries.put("scenario", this.getScenario().getName());
                reportEntries.put("status", "finished");
                context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
            }
        }

        return context;
    }

    public PicklejarEngineExecutionContext execute(PicklejarEngineExecutionContext context,
                                                   DynamicTestExecutor dynamicTestExecutor) {
        // execute steps
        for (StepDescriptor stepDescriptor : this.getStepDescriptors()) {
            logger.info("-".repeat(77));
            logger.info(() -> String.format("[%d] STEP \"%s\" STARTED",
                    Thread.currentThread().getId(),
                    stepDescriptor.getDisplayName()));
            logger.info("-".repeat(77));
            // execute @BeforeEachStep
            for (Class<?> c : context.getTestClassesFromFirstStepsPackage()) {
                for (Method method : AnnotationUtils.findAnnotatedMethods(c, BeforeEachStep.class, TOP_DOWN)) {
                    Preconditions.condition(method.getParameterCount() == 3,
                            "@BeforeEachStep method has wrong number of parameters");
                    ReflectionUtils.invokeMethod(method, ReflectionUtils.newInstance(c), testContext,
                            getScenario(), stepDescriptor.getStep());
                }
            }
            Instant endTime = null;
            Instant startTime = Instant.now();
            Map<String, String> reportEntries = new HashMap<>();
            reportEntries.put("step", stepDescriptor.getDisplayName());
            reportEntries.put("method", String.format("%s.%s",
                    stepDescriptor.getMethodDescriptor().getMethod().getDeclaringClass().getName(),
                    stepDescriptor.getMethodDescriptor().getMethod().getName())
            );
            reportEntries.put("status", StepExecutionResult.STARTED.toString());
            context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
            try {
                // Using the TestContext is optional
                if (testContext != null) {
                    ReflectionUtils.invokeMethod(stepDescriptor.getMethodDescriptor().getMethod(),
                            getDeclaringClassForMethod(stepDescriptor.getMethodDescriptor().getMethod(),
                                    testContext),
                            stepDescriptor.getMethodDescriptor().getParameters());
                } else {
                    ReflectionUtils.invokeMethod(stepDescriptor.getMethodDescriptor().getMethod(),
                            getDeclaringClassForMethod(stepDescriptor.getMethodDescriptor().getMethod()),
                            stepDescriptor.getMethodDescriptor().getParameters());
                }
            } catch (SkipException se) {
                logger.info(() -> String.format("[%d] SKIPPED",
                        Thread.currentThread().getId()));
                endTime = Instant.now();
                executionResult = TestExecutionResult.aborted(se);
                Result result = new Result(Duration.between(startTime, endTime).toNanos(), Result.SKIPPED, se.getMessage());
                stepDescriptor.getStep().setResult(result);
                reportEntries.put("status", result.getStatus());
                reportEntries.put("duration", String.valueOf(result.getDuration()));
                context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
                context.getExecutionListener().executionFinished(this, executionResult);
                // execute @AfterEachStep in case of skipped step
                for (Class<?> c : context.getTestClassesFromFirstStepsPackage()) {
                    for (Method method : AnnotationUtils.findAnnotatedMethods(c, AfterEachStep.class, TOP_DOWN)) {
                        Preconditions.condition(method.getParameterCount() == 3,
                                "@AfterEachStep method has wrong number of parameters");
                        ReflectionUtils.invokeMethod(method, ReflectionUtils.newInstance(c), testContext,
                                getScenario(), stepDescriptor.getStep());
                    }
                }
                // Cucumber report listener is not attached when test is executed via IntelliJ JUnit run configuration
                if (context.getCucumberReportGeneratingListener() != null) {
                    context.getCucumberReportGeneratingListener().stepExecutionFinished(stepDescriptor, result);
                }
                return context;
            } catch (Throwable e) {
                logger.info(() -> String.format("[%d] FAILED",
                        Thread.currentThread().getId()));
                endTime = Instant.now();
                executionResult = TestExecutionResult.failed(e);
                Result result = new Result(Duration.between(startTime, endTime).toNanos(), Result.FAILED,
                        getThrowableStacktraceString(e));
                stepDescriptor.getStep().setResult(result);
                reportEntries.put("status", result.getStatus());
                reportEntries.put("duration", String.valueOf(result.getDuration()));
                context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
                // execute @AfterEachStep in case of failed step
                for (Class<?> c : context.getTestClassesFromFirstStepsPackage()) {
                    for (Method method : AnnotationUtils.findAnnotatedMethods(c, AfterEachStep.class, TOP_DOWN)) {
                        Preconditions.condition(method.getParameterCount() == 3,
                                "@AfterEachStep method has wrong number of parameters");
                        ReflectionUtils.invokeMethod(method, ReflectionUtils.newInstance(c), testContext,
                                getScenario(), stepDescriptor.getStep());
                    }
                }
                // Cucumber report listener is not attached when test is executed via IntelliJ JUnit run configuration
                if (context.getCucumberReportGeneratingListener() != null) {
                    context.getCucumberReportGeneratingListener().stepExecutionFinished(stepDescriptor, result);
                }
                throw new RuntimeException(e);
            }
            logger.info(() -> String.format("[%d] PASSED",
                    Thread.currentThread().getId()));
            endTime = Instant.now();
            Result result = new Result(Duration.between(startTime, endTime).toNanos(), Result.PASSED, null);
            stepDescriptor.getStep().setResult(result);
            reportEntries.put("status", result.getStatus());
            reportEntries.put("duration", String.valueOf(result.getDuration()));
            context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
            // execute @AfterEachStep in case of successful step
            for (Class<?> c : context.getTestClassesFromFirstStepsPackage()) {
                for (Method method : AnnotationUtils.findAnnotatedMethods(c, AfterEachStep.class, TOP_DOWN)) {
                    Preconditions.condition(method.getParameterCount() == 3,
                            "@AfterEachStep method has wrong number of parameters");
                    ReflectionUtils.invokeMethod(method, ReflectionUtils.newInstance(c), testContext,
                            getScenario(), stepDescriptor.getStep());
                }
            }
            // Cucumber report listener is not attached when test is executed via IntelliJ JUnit run configuration
            if (context.getCucumberReportGeneratingListener() != null) {
                context.getCucumberReportGeneratingListener().stepExecutionFinished(stepDescriptor, result);
            }
        }

        executionResult = TestExecutionResult.successful();

        return context;
    }

    public void after(PicklejarEngineExecutionContext context) {
        logger.info("=".repeat(77));
        logger.info(() -> String.format("[%d] AFTER \"%s: %s\"",
                Thread.currentThread().getId(),
                this.getScenario().getFeature().getName(),
                this.getScenario().getName()));
        logger.info("=".repeat(77));

        Exception failedException = null;
        boolean failedInAfterEachScenario = false;

        // execute @AfterEachScenario
        for (Class<?> c : context.getTestClassesFromFirstStepsPackage()) {
            for (Method method : AnnotationUtils.findAnnotatedMethods(c, AfterEachScenario.class, TOP_DOWN)) {
                Instant endTime = null;
                Instant startTime = Instant.now();
                try {
                    Preconditions.condition(method.getParameterCount() == 2,
                            "@AfterEachScenario method has wrong number of parameters");
                    Map<String, String> reportEntries = new HashMap<>();
                    reportEntries.put("class", c.getCanonicalName() + "." + method.getName());
                    reportEntries.put("scenario", this.getScenario().getName());
                    reportEntries.put("status", "started");
                    context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
                    ReflectionUtils.invokeMethod(method, ReflectionUtils.newInstance(c), testContext,
                            getScenario());
                } catch (Exception e) {
                    endTime = Instant.now();
                    Result result = new Result(Duration.between(startTime, endTime).toNanos(), Result.FAILED,
                            e.getMessage());
                    this.getScenario().setAfter(new Around(result));
                    logger.severe(() -> "Exception in @AfterEachScenario");
                    Map<String, String> reportEntries = new HashMap<>();
                    reportEntries.put("class", c.getCanonicalName() + "." + method.getName());
                    reportEntries.put("scenario", this.getScenario().getName());
                    reportEntries.put("status", result.getStatus());
                    reportEntries.put("duration", String.valueOf(result.getDuration()));
                    context.getExecutionListener().reportingEntryPublished(this, ReportEntry.from(reportEntries));
                    executionResult = TestExecutionResult.failed(e);
                    failedException = e;
                    failedInAfterEachScenario = true;
                    context.getExecutionListener().executionFinished(this, executionResult);
                }
            }
        }
        // Cucumber report listener is not attached when test is executed via IntelliJ JUnit run configuration
        if (context.getCucumberReportGeneratingListener() != null) {
            context.getCucumberReportGeneratingListener().scenarioExecutionFinished(getScenario());
        }
        if (failedInAfterEachScenario) {
            throw new RuntimeException(failedException);
        }
    }

    private Object getDeclaringClassForMethod(final Method method, Object... constructorParams) throws
            InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
            InvocationTargetException {
        final Object declaringClassObject;
        final String declaringClassName = method.getDeclaringClass().getName();
        final List<Object> constructorParamsList = Arrays.asList(constructorParams);
        final List<Class<?>> constructorParamTypesList = constructorParamsList.stream()
                .map((object) -> object.getClass()).
                        collect(Collectors.toList());
        logger.fine(() -> String.format("[Execution] Step constructor param list size: %s", constructorParamsList.size()));
        logger.fine(() -> String.format("[Execution] Step constructor param type list size: %s", constructorParamTypesList.size()));
        logger.fine(() -> String.format("[Execution] Parameter types are: " + constructorParamsList
                .stream()
                .map(o -> o.getClass().getName())
                .collect(Collectors.joining(","))));

        // TODO: Try to use ReflectionUtils.findConstructors(method.getDeclaringClass(), ...) here
        final Constructor<?> ctor = method.getDeclaringClass().getConstructor(constructorParamTypesList.toArray(
                new Class<?>[constructorParamTypesList.size()]));
        declaringClassObject = method.getDeclaringClass().cast(ctor.newInstance(constructorParamsList.toArray()));

        classInstanceCache.put(declaringClassName, declaringClassObject);
        return declaringClassObject;
    }

    private String getThrowableStacktraceString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
