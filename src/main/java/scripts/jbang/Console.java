//SOURCES ./ConsoleColor.java

package scripts.jbang;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Console {

    public static ConsoleColor TITLE = ConsoleColor.BLACK_BOLD;
    public static ConsoleColor DEFAULT = ConsoleColor.DEFAULT;
    public static ConsoleColor FAILURE = ConsoleColor.RED;
    public static ConsoleColor SUCCESS = ConsoleColor.BLUE;
    public static ConsoleColor LOG_DEBUG = ConsoleColor.BLACK_BRIGHT;
    public static ConsoleColor LOG_ERROR = ConsoleColor.RED_BRIGHT;

    public static void print(String message, ConsoleColor consoleColor){
        System.out.print(String.format("%s%s%s", computeColor(consoleColor), message, ConsoleColor.RESET));
    }

    public static void println(String message, ConsoleColor consoleColor){
        System.out.println(String.format("%s%s%s", computeColor(consoleColor), message, ConsoleColor.RESET));
    }

    public static String getColoredMessage(String message, ConsoleColor consoleColor){
        return String.format("%s%s%s", computeColor(consoleColor), message, ConsoleColor.RESET);
    }

    private static String computeColor(ConsoleColor... consoleColor){
        return Arrays.stream(consoleColor).map(ConsoleColor::toString).collect(Collectors.joining());
    }
}
