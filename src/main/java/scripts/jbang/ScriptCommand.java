///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.projectlombok:lombok:1.18.24
//DEPS org.apache.commons:commons-lang3:3.12.0
//DEPS org.reflections:reflections:0.10.2
//DEPS info.picocli:picocli:4.7.0
//DEPS org.fusesource.jansi:jansi:2.4.0

//SOURCES ./CommandUtils.java
//SOURCES ./Console.java

//SOURCES ./psud/DockerPostgresCreate.java
//SOURCES ./psud/DockerPostgresCopy.java

package scripts.jbang;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import scripts.jbang.psud.DockerPostgresCopy;
import scripts.jbang.psud.DockerPostgresCreate;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

@FieldNameConstants
@CommandLine.Command(name = "", mixinStandardHelpOptions = true, version = "1.0.0",
        subcommands = {
                DockerPostgresCopy.class,
                DockerPostgresCreate.class
})
public class ScriptCommand implements Callable<Integer> {

    @Getter
    @CommandLine.Option(names = { "-i" }, description = "Mode interractif")
    public boolean interactiveMode;

    private static ScriptCommand scriptCommand = null;

    @Getter
    private StringBuilder printedCommand = new StringBuilder("$>");

    public  static final CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme.Builder()
            .commands    (CommandLine.Help.Ansi.Style.bold, CommandLine.Help.Ansi.Style.underline)
            .options     (CommandLine.Help.Ansi.Style.fg_yellow)
            .parameters  (CommandLine.Help.Ansi.Style.fg_yellow)
            .optionParams(CommandLine.Help.Ansi.Style.italic)
            .errors      (CommandLine.Help.Ansi.Style.fg_red, CommandLine.Help.Ansi.Style.bold)
            .stackTraces (CommandLine.Help.Ansi.Style.italic)
            .applySystemProperties()
            .build();

    public static ScriptCommand getInstance(){
        if (scriptCommand == null){
            scriptCommand = new ScriptCommand();
        }

        return scriptCommand;
    }

    public static void main(String... args) {


        int exitCode = new CommandLine(getInstance())
                .setExecutionStrategy(new CommandLine.RunAll())
                .setColorScheme(colorScheme)
                .execute(args);

        System.exit(exitCode);

    }

    @Override
    public Integer call() throws Exception {

        if (interactiveMode) {
            completePrintedCommand(this, ScriptCommand.Fields.interactiveMode, null);
        }

        return 0;
    }

    @SneakyThrows
    public void completePrintedCommand(Object classIntancce, String fieldName, Object value){

        Field field = classIntancce.getClass().getDeclaredField(fieldName);
        CommandLine.Option option = field.getAnnotation(CommandLine.Option.class);

        if (option != null){

            if (value != null && value.getClass().isAssignableFrom(Boolean.class)) {

                if (value.equals(Boolean.TRUE) && !option.negatable()){
                    printedCommand.append(" ");
                    printedCommand.append(option.names()[0]);
                }
                if (value.equals(Boolean.FALSE) && option.negatable()){
                    printedCommand.append(" ");
                    printedCommand.append(option.names()[0]);
                }
            }
            else if (value != null && !StringUtils.isBlank(value.toString())){
                printedCommand.append(" ");
                printedCommand.append(option.names()[0]);
                printedCommand.append("=");
                printedCommand.append(value);
            }
        }
    }

    public void completePrintedCommand(Object classIntancce){

        CommandLine.Command command = classIntancce.getClass().getAnnotation(CommandLine.Command.class);

        if (command != null) {
            printedCommand.append(" ");
            printedCommand.append(command.name());
        }
    }
}
