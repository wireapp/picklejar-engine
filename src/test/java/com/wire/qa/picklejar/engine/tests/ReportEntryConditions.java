package com.wire.qa.picklejar.engine.tests;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.Event;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.testkit.engine.Event.*;

public final class ReportEntryConditions {

    public static Condition<Event> reportEntry(String step, String status) {
        return new Condition<>(byPayload(ReportEntry.class, it -> it.getKeyValuePairs().get("step").equals(step)
                && it.getKeyValuePairs().get("status").equals(status)),
                "event for report entry with step = %s and status = %s", step, status);
    }

    public static Condition<Event> reportEntry(String step, String method, String status) {
        return new Condition<>(byPayload(ReportEntry.class, it -> it.getKeyValuePairs().get("step").equals(step)
                && it.getKeyValuePairs().get("method").equals(method)
                && it.getKeyValuePairs().get("status").equals(status)),
                "event for report entry with step = %s, method = %s and status = %s", step, method, status);
    }

    public static Condition<Event> reportEntry(String name, Class clasz, String methodName) {
        String trace = clasz.getName() + "." + methodName;
        return new Condition<>(byPayload(ReportEntry.class, it ->
                it.getKeyValuePairs().get(name).equals(trace)),
                "event for report entry with %s = %s", name, trace);
    }

    @SafeVarargs
    public static Condition<Event> reportEntry(Condition<? super ReportEntry>... conditions) {
        List<Condition<Event>> list = Arrays.stream(conditions)//
                .map(ReportEntryConditions::reportEntry)//
                .collect(toList());

        return Assertions.allOf(list);
    }

    public static Condition<Event> reportEntry(Condition<? super ReportEntry> condition) {
        return new Condition<>(byPayload(ReportEntry.class, condition::matches), "report entry where %s",
                condition);
    }

    public static Condition<ReportEntry> scenario(String name) {
        return new Condition<>(entry -> entry.getKeyValuePairs().containsKey("scenario")
                && entry.getKeyValuePairs().get("scenario").equals(name),
                "scenario is '%s'", name);
    }

    public static Condition<ReportEntry> step(String name) {
        return new Condition<>(entry -> entry.getKeyValuePairs().containsKey("step")
                && entry.getKeyValuePairs().get("step").equals(name),
                "step is '%s'", name);
    }

    public static Condition<ReportEntry> status(String value) {
        return new Condition<>(entry -> entry.getKeyValuePairs().containsKey("status")
                && entry.getKeyValuePairs().get("status").equals(value),
                "status is '%s'", value);
    }

    public static Condition<ReportEntry> clasz(Class clasz, String methodName) {
        String value = clasz.getName() + "." + methodName;
        return new Condition<>(entry -> entry.getKeyValuePairs().containsKey("class")
                && entry.getKeyValuePairs().get("class").equals(value),
                "class is '%s'", value);
    }

}
