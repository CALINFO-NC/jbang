package scripts.jbang;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Console {

    public static CommandLine.Help.Ansi.IStyle[]  DEFAULT = new CommandLine.Help.Ansi.IStyle[] {};
    public static CommandLine.Help.Ansi.IStyle[]  LOG_SUCCESS = new CommandLine.Help.Ansi.IStyle[] {CommandLine.Help.Ansi.Style.fg("1;5;1")};
    public static CommandLine.Help.Ansi.IStyle[]  LOG_DEBUG = new CommandLine.Help.Ansi.IStyle[] {CommandLine.Help.Ansi.Style.fg("1;1;1")};
    public static CommandLine.Help.Ansi.IStyle[]  LOG_ERROR = new CommandLine.Help.Ansi.IStyle[] {CommandLine.Help.Ansi.Style.fg("5;1;1")};

    public static void print(String message, CommandLine.Help.Ansi.IStyle... consoleColor){
        System.out.print(getColoredMessage(message, consoleColor));
    }

    public static void println(String message, CommandLine.Help.Ansi.IStyle... consoleColor){
        System.out.println(getColoredMessage(message, consoleColor));
    }

    public static String getColoredMessage(String message, CommandLine.Help.Ansi.IStyle... consoleColor){

        return String.format("%s%s%s", CommandLine.Help.Ansi.Style.on(consoleColor), message, CommandLine.Help.Ansi.Style.off(consoleColor));
    }

    public static String readConsoleValue(String message) throws Exception{
        return readConsoleValue(message, "");
    }

    public static String readConsoleValue(String message, String defaultValue) throws IOException {

        System.out.print(message);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result = reader.readLine();

        return StringUtils.isBlank(result) ? defaultValue : result;
    }

    public static String readConsoleValue(Object classInstance, String fieldStr, Object defaultValue, CommandLine.Help.Ansi.IStyle... color) throws Exception{

        Class<?> clazz = classInstance.getClass();
        Field field = clazz.getDeclaredField(fieldStr);

        CommandLine.Option option = field.getAnnotation(CommandLine.Option.class);
        String msg = String.join("\n", option.description());

        String strDefaultValue = null;
        if (defaultValue != null){
            strDefaultValue = defaultValue.toString();
        }

        if (!StringUtils.isBlank(strDefaultValue)){
            msg = String.format("%s (Par défaut %s)", msg, strDefaultValue);
        }
        msg = Console.getColoredMessage(String.format("%s : ", msg), color);
        return readConsoleValue(msg, strDefaultValue);

    }
}
