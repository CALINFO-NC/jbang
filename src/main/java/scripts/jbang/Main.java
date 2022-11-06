///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.projectlombok:lombok:1.18.24
//DEPS org.apache.commons:commons-lang3:3.12.0
//DEPS org.reflections:reflections:0.10.2

//SOURCES docker/psud/DockerPostgresCreate.java

package scripts.jbang;

import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static Map<String, Script> scripts = new HashMap<>();

    @SneakyThrows
    public static void main(String[] args) {

        initScripts();

        String startScript = "";

        while (!startScript.equals("exit")) {
            System.out.println();
            Console.print("Quel script souhaitez vous lancer (\"exit\" pour sortir \"list\" pour lister les scripts) :", ConsoleColor.BLACK_UNDERLINED);
            startScript = CommandUtils.readConsoleValue(" ");

            if (startScript.equals("exit")){
                break;
            }

            if (startScript.equals("list")){
                Console.println("Liste des script disponnibles : " , Console.SUCCESS);
                scripts.entrySet().forEach(e -> Console.println("    - " + e.getKey(), Console.SUCCESS));
                continue;
            }

            Script script = scripts.get(startScript);
            if (script == null){
                Console.println("Ce script n'existe pas.", Console.FAILURE);
            }
            else {
                try {
                    script.execute(args);
                } catch (Exception e) {
                    Console.println(getPrintValue(e), Console.FAILURE);
                }
            }

            startScript = "";
        }

        Console.println("Aurevoir !", Console.SUCCESS);
    }

    @SneakyThrows
    private static void initScripts(){

        Reflections reflections = new Reflections(Main.class.getPackageName(), new SubTypesScanner(false));

        List<Class<? extends Script>> lstClassScript = reflections.getSubTypesOf(Script.class)
                .stream()
                .collect(Collectors.toList());

        for (Class<? extends Script> classScript : lstClassScript){

            Constructor<? extends Script> scriptConstructor = classScript.getConstructor();
            Script scriptInstance = scriptConstructor.newInstance();
            scripts.put(scriptInstance.getCommandName(), scriptInstance);
        }
    }

    private static String getPrintValue(Throwable e) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return sw.toString();
    }
}
