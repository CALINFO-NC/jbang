package scripts.jbang;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandUtils {

    public static String readConsoleValue(String message) throws Exception{
        return readConsoleValue(message, "");
    }

    public static String readConsoleValue(String message, String defaultValue) throws IOException {

        System.out.print(message);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result = reader.readLine();

        return StringUtils.isBlank(result) ? defaultValue : result;
    }

    public static void executeCommand(String[] command) throws IOException {

        Console.println(String.format("$> %s", String.join(" ", command)), Console.LOG_DEBUG);

        Process p = Runtime.getRuntime().exec(command);

        try(BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

            String s;

            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                Console.println(s, Console.LOG_DEBUG);
            }

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                Console.println(s, Console.LOG_ERROR);
            }
        }
    }
}
