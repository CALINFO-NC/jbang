///usr/bin/env jbang "$0" "$@" ; exit $? 

try {

    println("START")

    executeScript = {command ->

        String strCmd = command.join(" ")
        println "Command : " + strCmd

        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    ant.input(addProperty: 'container', message: "Nom du container container sur lequel est installé la base de données : ")
    ant.input(addProperty: 'databaseName', message: "Nom de la base de données (par défaut ${grailsAppName}) : ", defaultvalue: "${grailsAppName}")
    ant.input(addProperty: 'host', message: "Nom DNS ou IP d'accès à la base de données depuis votre container (par défaut localhost) : ", defaultvalue: "localhost")
    ant.input(addProperty: 'port', message: "Port d'accès à la base de données depuis votre container (par défaut 5432) : ", defaultvalue: "5432")

    //Scripts a executer en administrateur
    String[] sqlSciptAdmin=[
            "CREATE USER ${ant.antProject.properties.databaseName} WITH PASSWORD '${ant.antProject.properties.databaseName}';", //création de l'utilisateur
            "CREATE DATABASE ${ant.antProject.properties.databaseName} OWNER ${ant.antProject.properties.databaseName};" //création de la base de donnée avec l'utilisateur en propriétaire
    ]

    //Scripts a executer  sur la base applicative
    String[] sqlScriptUsr =[
            "CREATE SCHEMA ${ant.antProject.properties.databaseName} AUTHORIZATION ${ant.antProject.properties.databaseName};", //création du schema applicatif
            "ALTER ROLE ${ant.antProject.properties.databaseName} SET search_path = ${ant.antProject.properties.databaseName}, public;", //le schema par default est celui de l'application
            "CREATE EXTENSION postgis;", //création de l'extension SIG
            "CREATE EXTENSION unaccent;", //création de l'extension unaccent
            "CREATE EXTENSION tsearch2;", //création de l'extension tsearch2 pour la recherche plein texte
            "CREATE EXTENSION pg_trgm;", //création de l'extension trigram pour la recherche insensible aux fautes
            "CREATE EXTENSION postgres_fdw;", //création de l'extension postgres_fdw pour crer des objets distant postgres
            "CREATE EXTENSION \"uuid-ossp\";", //création de l'extension uuid-ossp pour pouvoir générer des identifiant uuid
            """
                CREATE OR REPLACE FUNCTION public.first_agg ( anyelement, anyelement )
                    RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS \$\$
                        SELECT \$1;
                \$\$;
                CREATE AGGREGATE public.FIRST (
                        sfunc    = public.first_agg,
                        basetype = anyelement,
                        stype    = anyelement
                );
                
                CREATE OR REPLACE FUNCTION public.last_agg ( anyelement, anyelement )
                    RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS  \$\$
                        SELECT \$2;
                \$\$;

                CREATE AGGREGATE public.LAST (
                        sfunc    = public.last_agg,
                        basetype = anyelement,
                        stype    = anyelement
                );
            """.replaceAll("\n", "") //version non c-natif des fonctions aggrégative first/last
    ]

    sqlSciptAdmin.each {sql->
        String[] cmd = ["docker", "exec", "-i", "${ant.antProject.properties.container}", "psql", "-h", "${ant.antProject.properties.host}", "-p", "${ant.antProject.properties.port}", "-U", "postgres", "--no-password", "-c", "${sql}"]
        executeScript(cmd)
    }
    sqlScriptUsr.each {sql->
        String[] cmd = ["docker", "exec", "-i", "${ant.antProject.properties.container}", "psql", "-d", "${ant.antProject.properties.databaseName}", "-h", "${ant.antProject.properties.host}", "-p", "${ant.antProject.properties.port}", "-U", "postgres", "--no-password", "-c", "${sql}"]
        executeScript(cmd)
    }


    println("FIN")
}
finally {
    System.exit(0)
}
