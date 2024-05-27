# Pré-requis
* SDKMan https://sdkman.io/
* JBang https://www.jbang.dev/ (installation préconisé via SDKMan)
* JDK17 (intallation préconisé via SDKMan)

# Lancement du script

Cette ligne de commande vous donne l'aide afin de voir tous les commandes pouvant être lancée
```
jbang https://github.com/CALINFO-NC/jbang/blob/bef7e1313d6c8d728190b445e26537f823eec7d3/src/main/java/scripts/jbang/ScriptCommand.java --help
```

Si vous lancée une commande comme par exemple *psud-docker-postgres-copy* vous pouvez utiliser l'aide de cette
commande en faisant

```
jbang https://github.com/CALINFO-NC/jbang/blob/bef7e1313d6c8d728190b445e26537f823eec7d3/src/main/java/scripts/jbang/ScriptCommand.java psud-docker-postgres-copy --help
```

Il existe aussi un mode interractif pour les comandes en mettant *-i*. Par exemple si vous voulez lancer la commande *psud-docker-postgres-copy*
en mode intéractif, il vous suffit de lancer la commande comme ceci 

```
jbang https://github.com/CALINFO-NC/jbang/blob/bef7e1313d6c8d728190b445e26537f823eec7d3/src/main/java/scripts/jbang/ScriptCommand.java -i psud-docker-postgres-copy
```
