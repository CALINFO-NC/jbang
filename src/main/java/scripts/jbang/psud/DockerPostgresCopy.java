//SOURCES ../CommandUtils.java
//SOURCES ../Console.java
//SOURCES ../ScriptCallable.java
//SOURCES ../ScriptCommand.java

package scripts.jbang.psud;

import lombok.experimental.FieldNameConstants;
import picocli.CommandLine;
import scripts.jbang.CommandUtils;
import scripts.jbang.Console;
import scripts.jbang.ScriptCallable;
import scripts.jbang.ScriptCommand;

@FieldNameConstants
@CommandLine.Command(name = "psud-docker-postgres-copy", mixinStandardHelpOptions = true, version = "1.0.0", description = "Cette commande va copier une base de données d'un environnement PSud vers un autre environnent")
public class DockerPostgresCopy implements ScriptCallable {

    @CommandLine.Option(names = { "--container" }, paramLabel = "CONTAINER", description = "Nom du container sur lequel un postgreSql est installé")
    private String container;

    @CommandLine.Option(names = { "--host-src" }, paramLabel = "HOST_SRC", description = "Nom DNS ou IP de la base de données source", defaultValue = "pgsql1-qualif.province-sud.qualif")
    private String hostSrc;

    @CommandLine.Option(names = { "--port-src" }, paramLabel = "PORT_SRC", description = "Port d'accès à la base de données source", defaultValue = "5432")
    private int portSrc;

    @CommandLine.Option(names = { "--database-name-src" }, paramLabel = "DB_NAME_SRC", description = "Nom de la base de données source")
    private String databaseNameSrc;

    @CommandLine.Option(names = { "--host-dst" }, paramLabel = "HOST_DST", description = "Nom DNS ou IP de la base de données de destination", defaultValue = "localhost")
    private String hostDst;

    @CommandLine.Option(names = { "--port-dst" }, paramLabel = "PORT_DST", description = "Port d'accès à la base de données de destination", defaultValue = "5432")
    private int portDst;

    @CommandLine.Option(names = { "--database-name-dst" }, paramLabel = "DB_NAME_DST", description = "Nom de la base de données de destination")
    private String databaseNameDst;

    @CommandLine.Option(names = { "-b", "--blobs" }, description = "Copie des blobs")
    private boolean blob;

    @Override
    public void callScript() throws Exception{

        if (ScriptCommand.getInstance().isInteractiveMode()) {

            container = Console.readConsoleValue(this, Fields.container, container, Console.DEFAULT);
            hostSrc = Console.readConsoleValue(this, Fields.hostSrc, hostSrc, Console.DEFAULT);
            portSrc = Integer.parseInt(Console.readConsoleValue(this, Fields.portSrc, portSrc, Console.DEFAULT));
            databaseNameSrc = Console.readConsoleValue(this, Fields.databaseNameSrc, databaseNameSrc, Console.DEFAULT);
            hostDst = Console.readConsoleValue(this, Fields.hostDst, hostDst, Console.DEFAULT);
            portDst = Integer.parseInt(Console.readConsoleValue(this, Fields.portDst, portDst, Console.DEFAULT));
            databaseNameDst = Console.readConsoleValue(this, Fields.databaseNameDst, databaseNameSrc, Console.DEFAULT);
            String strblob = Console.readConsoleValue(this, Fields.blob, blob ? "true" : "false", Console.DEFAULT);
            blob = strblob.equalsIgnoreCase("true") ? true : false;
        }


        ScriptCommand.getInstance().completePrintedCommand(this);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.container, container);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.hostSrc, hostSrc);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.portSrc, portSrc);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.databaseNameSrc, databaseNameSrc);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.hostDst, hostDst);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.portDst, portDst);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.databaseNameDst, databaseNameDst);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.blob, blob);

        String filaName = "/tmp/bud-" + System.nanoTime() + ".backup";

        String[] cmd = new String[] {"docker", "exec", "-e", String.format("PGPASSWORD=%s", databaseNameSrc), "-i", container, "pg_dump", "--clean", "--blobs", String.format("--host=%s", hostSrc), String.format("--port=%s", portSrc), String.format("--username=%s", databaseNameSrc), "--compress=9", "--format=c", String.format("--schema=%s", databaseNameSrc), String.format("--file=%s", filaName), databaseNameSrc};
        if (blob){
            cmd = new String[] {"docker", "exec", "-e", String.format("PGPASSWORD=%s", databaseNameSrc), "-i", container, "pg_dump", "--clean", String.format("--host=%s", hostSrc), String.format("--port=%s", portSrc), String.format("--username=%s", databaseNameSrc), "--compress=9", "--format=c", String.format("--schema=%s", databaseNameSrc), String.format("--file=%s", filaName), databaseNameSrc};
        }
        CommandUtils.executeCommand(cmd);

        cmd = new String[] {"docker", "exec", "-e", String.format("PGPASSWORD=%s", databaseNameDst), "-i", container, "pg_restore", "--clean", String.format("--dbname=%s", databaseNameDst), String.format("--host=%s", hostDst), "-p", String.format("%s", portDst), String.format("--username=%s", databaseNameDst), filaName};
        CommandUtils.executeCommand(cmd);

    }
}
