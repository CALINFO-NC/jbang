//SOURCES ./ConsoleColor.java

package scripts.jbang;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Console {


    public static boolean PROMPT_COLOR = true;

    public static ConsoleColor TITLE = ConsoleColor.BLACK_BOLD;
    public static ConsoleColor DEFAULT = ConsoleColor.DEFAULT;
    public static ConsoleColor FAILURE = ConsoleColor.RED;
    public static ConsoleColor SUCCESS = ConsoleColor.BLUE;
    public static ConsoleColor LOG_DEBUG = ConsoleColor.BLACK_BRIGHT;
    public static ConsoleColor LOG_ERROR = ConsoleColor.RED_BRIGHT;

    public static void print(String message, ConsoleColor consoleColor){
        System.out.print(getColoredMessage(message, consoleColor));
    }

    public static void println(String message, ConsoleColor consoleColor){
        System.out.println(getColoredMessage(message, consoleColor));
    }

    public static String getColoredMessage(String message, ConsoleColor consoleColor){

        if (PROMPT_COLOR) {
            return String.format("%s%s%s", computeColor(consoleColor), message, ConsoleColor.RESET);
        }
        else{
            return message;
        }
    }

    private static String computeColor(ConsoleColor... consoleColor){

        if (PROMPT_COLOR)
            return Arrays.stream(consoleColor).map(ConsoleColor::toString).collect(Collectors.joining());
        else{
            return Arrays.stream(consoleColor).map(ConsoleColor::toString).collect(Collectors.joining());
        }
    }
}
