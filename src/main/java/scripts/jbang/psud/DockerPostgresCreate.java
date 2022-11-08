//SOURCES ../CommandUtils.java
//SOURCES ../Console.java
//SOURCES ../ScriptCallable.java

package scripts.jbang.psud;

import lombok.experimental.FieldNameConstants;
import picocli.CommandLine;
import scripts.jbang.CommandUtils;
import scripts.jbang.Console;
import scripts.jbang.ScriptCallable;
import scripts.jbang.ScriptCommand;

@FieldNameConstants
@CommandLine.Command(name = "psud-docker-postgres-create", mixinStandardHelpOptions = true, version = "1.0.0", description = "Cette commande va préparer une base de données compatible avec les application de la province sud")
public class DockerPostgresCreate implements ScriptCallable {


    @CommandLine.Option(names = { "-c", "--container" }, paramLabel = "CONTAINER", description = "Nom du container sur lequel un postgreSql est installé")
    private String container;

    @CommandLine.Option(names = { "-H", "--host" }, paramLabel = "CONTAINER", description = "Nom DNS ou IP d'accès à la base de données (par défaut localhost)", defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = { "-p", "--port" }, paramLabel = "CONTAINER", description = "Port d'accès à la base de données (par défaut 5432)", defaultValue = "5432")
    private int port;

    @CommandLine.Option(names = { "-dn", "--database-name" }, paramLabel = "CONTAINER", description = "Nom de la base de données")
    private String databaseName;

    @Override
    public void callScript() throws Exception {

        if (ScriptCommand.getInstance().isInteractiveMode()) {

            container = Console.readConsoleValue(this, Fields.container, container, Console.DEFAULT);
            databaseName = Console.readConsoleValue(this, Fields.databaseName, databaseName, Console.DEFAULT);
            host = Console.readConsoleValue(this, Fields.host, host, Console.DEFAULT);
            port = Integer.parseInt(Console.readConsoleValue(this, Fields.port, port, Console.DEFAULT));
        }


        ScriptCommand.getInstance().completePrintedCommand(this);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.container, container);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.databaseName, databaseName);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.host, host);
        ScriptCommand.getInstance().completePrintedCommand(this, Fields.port, port);


        String[] sqlSciptAdmin = new String[]{
                String.format("CREATE USER %s WITH PASSWORD '%s';", databaseName, databaseName),
                String.format("CREATE DATABASE %s OWNER %s;", databaseName, databaseName)
        };

        String[] sqlScriptUsr = new String[]{
                String.format("CREATE SCHEMA %s AUTHORIZATION %s;", databaseName, databaseName), //création du schema applicatif
                String.format("ALTER ROLE %s SET search_path = %s, public;", databaseName, databaseName), //le schema par default est celui de l'application
                String.format("CREATE EXTENSION postgis;"),
                String.format("CREATE EXTENSION unaccent;"),
                String.format("CREATE EXTENSION tsearch2;"),
                String.format("CREATE EXTENSION pg_trgm;"),
                String.format("CREATE EXTENSION postgres_fdw;"),
                String.format("CREATE EXTENSION \"uuid-ossp\";"),
                String.format("""
                    CREATE OR REPLACE FUNCTION public.first_agg ( anyelement, anyelement )
                        RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS $$
                            SELECT $1;
                    $$;
                    CREATE AGGREGATE public.FIRST (
                            sfunc    = public.first_agg,
                            basetype = anyelement,
                            stype    = anyelement
                    );
                    
                    CREATE OR REPLACE FUNCTION public.last_agg ( anyelement, anyelement )
                        RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS  $$
                            SELECT $2;
                    $$;
                    CREATE AGGREGATE public.LAST (
                            sfunc    = public.last_agg,
                            basetype = anyelement,
                            stype    = anyelement
                    );
                """.replaceAll("\n", ""))
        };

        for (String sql : sqlSciptAdmin){
            String[] cmd = new String[] {"docker", "exec", "-i", container, "psql", "-h", host, "-p", String.format("%s", port), "-U", "postgres", "--no-password", "-c", sql};
            CommandUtils.executeCommand(cmd);
        }


        for (String sql : sqlScriptUsr){
            String[] cmd = new String[] {"docker", "exec", "-i", container, "psql", "-d", databaseName, "-h", host, "-p", String.format("%s", port), "-U", "postgres", "--no-password", "-c", sql};
            CommandUtils.executeCommand(cmd);
        }
    }
}
