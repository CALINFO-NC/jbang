package scripts.jbang;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandUtils {

    public static void executeCommand(String[] commandNonFormate) throws IOException {


        List<String> lstCommand = Arrays.stream(commandNonFormate).map(i -> String.format("%s", i)).collect(Collectors.toList());
        String[] command = lstCommand.toArray(new String[0]);

        Console.println(String.format("$> %s", String.join(" ", command)), Console.LOG_DEBUG);

        Process p = Runtime.getRuntime().exec(command);

        try(BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

            String s;

            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                Console.println(s, Console.LOG_SUCCESS);
            }

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                Console.println(s, Console.LOG_ERROR);
            }
        }
    }
}
