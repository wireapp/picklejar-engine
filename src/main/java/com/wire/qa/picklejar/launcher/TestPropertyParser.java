package com.wire.qa.picklejar.launcher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPropertyParser {

    private static final Map<Character, String> encodedCharacterMap = new HashMap<>();

    static {
        // According to org.junit.platform.engine.UniqueIdFormat following chars are not allowed in unique ids:
        final char[] forbiddenCharacters = new char[]{ '[', ':', ']', '/' };
        for (char c: forbiddenCharacters) {
            encodedCharacterMap.put(c, URLEncoder.encode(String.valueOf(c), StandardCharsets.UTF_8));
        }
    }

    private static class Token {
        private final String text;
        private final int start;
        private final int end;

        public Token(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return text + ", " + start + ", " + end;
        }
    }

    public static List<String> parse(String testProperty) {
        List<String> uniqueIds = new ArrayList<>();

        List<Token> featureTokens = new ArrayList<>();
        Pattern p = Pattern.compile("([a-zA-Z0-9 /-]+)#");
        Matcher m = p.matcher(testProperty);
        while (m.find()) {
            Token token = new Token(m.group(1), m.start(1), m.end(1));
            featureTokens.add(token);
        }

        for (int i = 0; i < featureTokens.size(); i++) {
            Token currentFeatureToken = featureTokens.get(i);
            int startOfNextToken = testProperty.length();
            if (i + 1 < featureTokens.size()) {
                startOfNextToken = featureTokens.get(i + 1).start - 1;
            }
            String scenariosSegment = testProperty.substring(currentFeatureToken.end + 1, startOfNextToken);
            String[] scenarios = scenariosSegment.split("\\+");
            for (String scenario : scenarios) {
                if (scenario.matches("(.*) [0-9]+$")) {
                    String examplePart = scenario.substring(scenario.lastIndexOf(" ") + 1);
                    uniqueIds.add(String.format("[engine:picklejar-engine]/[Feature:%s]/[Scenario:%s]/[Example:%s]",
                            sanitizeForUniqueId(currentFeatureToken.text),
                            sanitizeForUniqueId(scenario.substring(0, scenario.lastIndexOf(" "))),
                            examplePart));
                } else {
                    uniqueIds.add(String.format("[engine:picklejar-engine]/[Feature:%s]/[Scenario:%s]",
                            sanitizeForUniqueId(currentFeatureToken.text),
                            sanitizeForUniqueId(scenario)));
                }
            }
        }

        return uniqueIds;
    }

    private static String sanitizeForUniqueId(String s) {
        StringBuilder builder = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String value = encodedCharacterMap.get(c);
            if (value == null) {
                builder.append(c);
                continue;
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
