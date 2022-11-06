//SOURCES ../CommandUtils.java
//SOURCES ../Console.java
//SOURCES ../Script.java

package scripts.jbang.psud;

import scripts.jbang.CommandUtils;
import scripts.jbang.Console;
import scripts.jbang.Script;

public class DockerPostgresCopy implements Script {

    public String getCommandName(){
        return "psud-docker-postgres-copy";
    }

    @Override
    public void execute(String[] args) throws Exception {

        Console.println("Ce script va copier une base de données d'un environnement PSud vers un autre environnent", Console.TITLE);

        String container = CommandUtils.readConsoleValue(Console.getColoredMessage("Nom du container sur lequel un postgreSql est installé : ", Console.DEFAULT));
        String hostSrc = CommandUtils.readConsoleValue(Console.getColoredMessage("Nom DNS ou IP de la base de données source (par défaut pgsql1-qualif.province-sud.qualif) : ", Console.DEFAULT), "pgsql1-qualif.province-sud.qualif");
        String portSrc = CommandUtils.readConsoleValue(Console.getColoredMessage("Port d'accès à la base de données source (par défaut 5432) : ", Console.DEFAULT), "5432");
        String databaseNameSrc = CommandUtils.readConsoleValue(Console.getColoredMessage("Nom de la base de données source : ", Console.DEFAULT));

        String hostDst = CommandUtils.readConsoleValue(Console.getColoredMessage("Nom DNS ou IP de la base de données de destination (par défaut localhost) : ", Console.DEFAULT), "localhost");
        String portDst = CommandUtils.readConsoleValue(Console.getColoredMessage("Port d'accès à la base de données de destination (par défaut 5432) : ", Console.DEFAULT), "5432");
        String databaseNameDst = CommandUtils.readConsoleValue(Console.getColoredMessage(String.format("Nom de la base de données de destination (par défaut %s) : ", databaseNameSrc), Console.DEFAULT), databaseNameSrc);

        String bolb = CommandUtils.readConsoleValue(Console.getColoredMessage("Souhaitez vous copier les blobs oui/non (oui par défaut) : ", Console.DEFAULT), "oui");

        String filaName = "/tmp/bud-" + System.nanoTime() + ".backup";

        String[] cmd = new String[] {"docker", "exec", "-e", String.format("PGPASSWORD=%s", databaseNameSrc), "-i", container, "pg_dump", "--clean", "--blobs", String.format("--host=%s", hostSrc), String.format("--port=%s", portSrc), String.format("--username=%s", databaseNameSrc), "--compress=9", "--format=c", String.format("--schema=%s", databaseNameSrc), String.format("--file=%s", filaName), databaseNameSrc};
        if (!bolb.equalsIgnoreCase("oui")){
            cmd = new String[] {"docker", "exec", "-e", String.format("PGPASSWORD=%s", databaseNameSrc), "-i", container, "pg_dump", "--clean", String.format("--host=%s", hostSrc), String.format("--port=%s", portSrc), String.format("--username=%s", databaseNameSrc), "--compress=9", "--format=c", String.format("--schema=%s", databaseNameSrc), String.format("--file=%s", filaName), databaseNameSrc};
        }
        CommandUtils.executeCommand(cmd);

        cmd = new String[] {"docker", "exec", "-e", String.format("PGPASSWORD=%s", databaseNameDst), "-i", container, "pg_restore", "--clean", String.format("--dbname=%s", databaseNameDst), String.format("--host=%s", hostDst), "-p", portDst, String.format("--username=%s", databaseNameDst), filaName};
        CommandUtils.executeCommand(cmd);

    }
}
