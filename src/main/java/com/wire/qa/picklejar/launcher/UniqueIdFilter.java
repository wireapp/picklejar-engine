package com.wire.qa.picklejar.launcher;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.PostDiscoveryFilter;

import static java.util.Arrays.asList;

/**
 * This class is almost a copy of JUnit5's org.junit.platform.launcher.TagFilter
 */
public final class UniqueIdFilter {

    private UniqueIdFilter() {
        /* no-op */
    }

    public static PostDiscoveryFilter includeIds(String... idExpressions) throws PreconditionViolationException {
        Preconditions.notNull(idExpressions, "array of ids expressions must not be null");
        return includeIds(asList(idExpressions));
    }

    public static PostDiscoveryFilter includeIds(List<String> idExpressions) throws PreconditionViolationException {
        return includeMatching(idExpressions, Stream::anyMatch);
    }

    public static PostDiscoveryFilter excludeIds(String... idExpressions) throws PreconditionViolationException {
        Preconditions.notNull(idExpressions, "array of ids expressions must not be null");
        return excludeIds(asList(idExpressions));
    }

    public static PostDiscoveryFilter excludeIds(List<String> tagExpressions) throws PreconditionViolationException {
        return includeMatching(tagExpressions, Stream::noneMatch);
    }

    private static PostDiscoveryFilter includeMatching(List<String> idExpressions,
                                                       BiPredicate<Stream<String>, Predicate<String>> matcher) {

        Preconditions.notEmpty(idExpressions, "list of id expressions must not be null or empty");
        return descriptor -> {
            UniqueId uniqueId = descriptor.getUniqueId();
            return FilterResult.includedIf(
                    matcher.test(idExpressions.stream(), expression -> expression.equals(uniqueId.toString())));
        };
    }

}