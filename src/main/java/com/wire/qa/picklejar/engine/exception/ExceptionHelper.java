package com.wire.qa.picklejar.engine.exception;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Logger;

public class ExceptionHelper {

    private static final Logger logger = Logger.getLogger(ExceptionHelper.class.getName());

    public static int getLineNumberInFeature(File file, String line) {
        int lineNumber = 0;
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                lineNumber += 1;
                if (scanner.nextLine().contains(line)) {
                    return lineNumber;
                }
            }
        } catch (FileNotFoundException e) {
            logger.warning("[ExceptionHelper] Could not find line number in feature file.");
        }
        return 1;
    }
}
