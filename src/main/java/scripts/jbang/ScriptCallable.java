package scripts.jbang;

import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

public interface ScriptCallable extends Callable <Integer>{

    void callScript() throws Exception;

    @Override
    default Integer call() {
        try {
            callScript();
            Console.println(ScriptCommand.getInstance().getPrintedCommand().toString(), CommandLine.Help.Ansi.Style.bg_yellow, CommandLine.Help.Ansi.Style.fg_black);
            return 0;
        }
        catch (Exception e){

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            Console.println(ScriptCommand.getInstance().getPrintedCommand().toString(), CommandLine.Help.Ansi.Style.bg_yellow, CommandLine.Help.Ansi.Style.fg_black);
            Console.println(sw.toString(), Console.LOG_ERROR);

            return -1;
        }
    }
}
