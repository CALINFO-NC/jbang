//SOURCES ../../CommandUtils.java
//SOURCES ../../Console.java
//SOURCES ../../Script.java

package scripts.jbang.docker.psud;

import scripts.jbang.CommandUtils;
import scripts.jbang.Console;
import scripts.jbang.Script;

public class DockerPostgresCreate implements Script {

    public String getCommandName(){
        return "psud-docker-postgres-create";
    }

    @Override
    public void execute(String[] args) throws Exception {

        Console.println("Ce script va préparer une base de données compatible avec les application de la province sud", Console.TITLE);

        String container = CommandUtils.readConsoleValue(Console.getColoredMessage("Nom du container container sur lequel un postgreSql est installé : ", Console.DEFAULT));
        String databaseName = CommandUtils.readConsoleValue(Console.getColoredMessage("Nom de la base de données : ", Console.DEFAULT));
        String host = CommandUtils.readConsoleValue(Console.getColoredMessage("Nom DNS ou IP d'accès à la base de données (par défaut localhost) : ", Console.DEFAULT), "localhost");
        String port = CommandUtils.readConsoleValue(Console.getColoredMessage("Port d'accès à la base de données (par défaut 5432) : ", Console.DEFAULT), "5432");

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
            String[] cmd = new String[] {"docker", "exec", "-i", container, "psql", "-h", host, "-p", port, "-U", "postgres", "--no-password", "-c", sql};
            CommandUtils.executeCommand(cmd);
        }


        for (String sql : sqlScriptUsr){
            String[] cmd = new String[] {"docker", "exec", "-i", container, "psql", "-d", databaseName, "-h", host, "-p", port, "-U", "postgres", "--no-password", "-c", sql};
            CommandUtils.executeCommand(cmd);
        }
    }
}
