package scripts.jbang.service;

import com.sun.source.tree.*;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import picocli.CommandLine;
import scripts.jbang.Console;
import scripts.jbang.ScriptCallable;
import scripts.jbang.ScriptCommand;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@FieldNameConstants
@CommandLine.Command(name = "service-crud-create", mixinStandardHelpOptions = true, version = "1.0.0", description = "Cette commande va créer le crud associé à une entité")
public class ServiceCrudCreate implements ScriptCallable {

    @CommandLine.Option(names = { "--project-path" }, paramLabel = "PROJECT_PATH", description = "Chemin du fichier")
    private String projectPath;

    @CommandLine.Option(names = { "--project-name" }, paramLabel = "PROJECT_NAME", description = "Nom du projet")
    private String projectName;

    @CommandLine.Option(names = { "--class-name" }, paramLabel = "CLASS_NAME", description = "Nom de la classe sans le stéréotype. Par exemple Contact et non ContactResource")
    private String className;

    @CommandLine.Option(names = { "--ss-package-fonctionnel" }, paramLabel = "SS_PACKAGE_FONCTIONNEL", description = "Package fonctionnel, par exemple contact")
    private String ssPkgFonctionnel;

    @CommandLine.Option(names = { "--tenant" }, paramLabel = "TENANT", description = "Tenant domain/generic")
    private String tenant;

    @Override
    public void callScript() throws Exception {

        if (ScriptCommand.getInstance().isInteractiveMode()) {

            projectPath = Console.readConsoleValue(this, ServiceCrudCreate.Fields.projectPath, projectPath, Console.DEFAULT);
            projectName = Console.readConsoleValue(this, ServiceCrudCreate.Fields.projectName, projectName, Console.DEFAULT);
            className = Console.readConsoleValue(this, ServiceCrudCreate.Fields.className, className, Console.DEFAULT);
            ssPkgFonctionnel = Console.readConsoleValue(this, ServiceCrudCreate.Fields.ssPkgFonctionnel, ssPkgFonctionnel, Console.DEFAULT);
            tenant = Console.readConsoleValue(this, ServiceCrudCreate.Fields.tenant, tenant, Console.DEFAULT);
        }

        ScriptCommand.getInstance().completePrintedCommand(this);
        ScriptCommand.getInstance().completePrintedCommand(this, ServiceCrudCreate.Fields.projectPath, projectPath);
        ScriptCommand.getInstance().completePrintedCommand(this, ServiceCrudCreate.Fields.projectName, projectName);
        ScriptCommand.getInstance().completePrintedCommand(this, ServiceCrudCreate.Fields.className, className);
        ScriptCommand.getInstance().completePrintedCommand(this, ServiceCrudCreate.Fields.ssPkgFonctionnel, ssPkgFonctionnel);
        ScriptCommand.getInstance().completePrintedCommand(this, ServiceCrudCreate.Fields.tenant, tenant);

        generateJavaService();
        generateJavaEntity();
        generateJavaRepository();
        generateJavaConverter();
        generateJavaServiceImpl();
        generateJavaController();
        generateAngularModel();
        generateAngularService();
        generateAngularListTs();
        generateAngularListHtml();
        generateAngularListScss();
        generateAngularFormTs();
        generateAngularFormHtml();
        generateAngularFormScss();
        generateAngularResolverTs();
    }


    public void generateJavaEntity(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        velocityContext.put("classNameHyphens", transformToHyphens(className, "_", false));

        String pkg = fillVelocityTemplate("com.calinfo.api.${projectName.toLowerCase()}.metier.${ssPkgFonctionnel}.entity.${tenant}", velocityContext);
        velocityContext.put("pkg", pkg);

        writeJavaFile(pkg, "Entity", velocityContext);
    }

    public void generateJavaController(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        velocityContext.put("classNameUrl", transformToHyphens(className, "-", true));

        String pkg = fillVelocityTemplate("com.calinfo.api.${projectName.toLowerCase()}.metier.${ssPkgFonctionnel}.controller", velocityContext);
        velocityContext.put("pkg", pkg);

        writeJavaFile(pkg, "Controller", velocityContext);
    }

    public void generateJavaServiceImpl(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        String pkg = fillVelocityTemplate("com.calinfo.api.${projectName.toLowerCase()}.metier.${ssPkgFonctionnel}.service.impl", velocityContext);
        velocityContext.put("pkg", pkg);

        writeJavaFile(pkg, "ServiceImpl", velocityContext);
    }

    public void generateJavaService(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        String pkg = fillVelocityTemplate("com.calinfo.api.${projectName.toLowerCase()}.metier.${ssPkgFonctionnel}", velocityContext);
        velocityContext.put("pkg", pkg);

        writeJavaFile(pkg, "Service", velocityContext);
    }

    public void generateJavaRepository(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        String pkg = fillVelocityTemplate("com.calinfo.api.${projectName.toLowerCase()}.metier.${ssPkgFonctionnel}.repository.${tenant}", velocityContext);
        velocityContext.put("pkg", pkg);

        writeJavaFile(pkg, "Repository", velocityContext);
    }

    public void generateJavaConverter(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        String pkg = fillVelocityTemplate("com.calinfo.api.${projectName.toLowerCase()}.metier.${ssPkgFonctionnel}.converter", velocityContext);
        velocityContext.put("pkg", pkg);

        writeJavaFile(pkg, "EntityResourceConverter", velocityContext);
    }

    public void generateAngularModel(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        writeAngularFile("model.ts", "services", velocityContext);
    }

    public void generateAngularService(){



        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);
        velocityContext.put("classNameUrl", transformToHyphens(className, "-", true));
        velocityContext.put("classNameHyphens", transformToHyphens(className, "-", false));

        writeAngularFile("service.ts", "services", velocityContext);

    }

    public void generateAngularListTs(){


        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);
        velocityContext.put("classNameHyphens", transformToHyphens(className, "-", false));

        writeAngularFile("list.component.ts", "views", velocityContext);

    }

    public void generateAngularListHtml(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        writeAngularFile("list.component.html", "views", velocityContext);
    }

    public void generateAngularListScss(){

        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        writeAngularFile("list.component.scss", "views", velocityContext);
    }

    public void generateAngularFormTs(){


        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);
        velocityContext.put("classNameHyphens", transformToHyphens(className, "-", false));

        writeAngularFile("form.component.ts", "views", velocityContext);
    }

    public void generateAngularFormHtml(){


        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        writeAngularFile("form.component.html", "views", velocityContext);

    }

    public void generateAngularFormScss(){


        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);

        writeAngularFile("form.component.scss", "views", velocityContext);
    }

    public void generateAngularResolverTs(){


        VelocityContext velocityContext = new VelocityContext();
        fillDefaultVelocityContext(velocityContext);
        velocityContext.put("classNameHyphens", transformToHyphens(className, "-", false));

        writeAngularFile("resolver.ts", "views", velocityContext);
    }

    @SneakyThrows
    private String fillVelocityTemplate(String template, VelocityContext context){

        try(StringWriter writer = new StringWriter();
            StringReader reader = new StringReader(template);) {
            Velocity.evaluate(context, writer, "JBang", reader);
            return writer.toString();
        }
    }

    @SneakyThrows
    private String getContentVelocityTemplateFromJavaFile(String fileName){
        File file = new File(projectPath + "/jbang/templates/java/" + fileName);
        return getContentVelocityTemplateFromFile(file);
    }

    @SneakyThrows
    private String getContentVelocityTemplateFromAngularFile(String fileName){
        File file = new File(projectPath + "/jbang/templates/angular/" + fileName);
        return getContentVelocityTemplateFromFile(file);
    }

    @SneakyThrows
    private String getContentVelocityTemplateFromFile(File file){
        String templateContent = "";
        try(FileInputStream fin = new FileInputStream(file);
            ByteArrayOutputStream bout = new ByteArrayOutputStream()){
            IOUtils.copy(fin, bout);
            templateContent = new String(bout.toByteArray());
        }

        return templateContent;
    }

    private void fillDefaultVelocityContext(VelocityContext velocityContext){
        velocityContext.put("projectName", projectName);
        velocityContext.put("ssPkgFonctionnel", ssPkgFonctionnel);
        velocityContext.put("className", className);
        velocityContext.put("tenant", tenant);

        String pkgApi = fillVelocityTemplate("com.calinfo.api.${projectName.toLowerCase()}.metier.${ssPkgFonctionnel}", velocityContext);
        String baseApiPath = getJavaApiPackagePath(pkgApi);
        List<FieldProperty> lst = ClassPropertiesUtils.getFieldsFromJavaClassFile(new File(baseApiPath + "/" + className + "Resource.java"));
        velocityContext.put("fields", lst);
    }

    private String getJavaPackagePath(String pkg){
        return projectPath + "/" + projectName.toLowerCase(Locale.ROOT) + "/src/main/java/" + pkg.replace(".", "/");
    }

    private String getJavaApiPackagePath(String pkg){
        return projectPath + "/" + projectName.toLowerCase(Locale.ROOT) + "-api/src/main/java/" + pkg.replace(".", "/");
    }

    private String getBaseAngularPath(){
        return projectPath + "/" + projectName.toLowerCase(Locale.ROOT) + "/src/main/angular/" + projectName.toLowerCase(Locale.ROOT) + "/src/app/";
    }

    @SneakyThrows
    private void writeFile(Path path, String content){

        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes());
    }

    public void writeJavaFile(String pkg, String stereotype, VelocityContext velocityContext){
        String basePath = getJavaPackagePath(pkg);

        if (Objects.equals(stereotype, "Service")){
            basePath = getJavaApiPackagePath(pkg);
        }

        Path path = Paths.get(basePath + "/" + className + stereotype + ".java");
        writeFile(path, fillVelocityTemplate(getContentVelocityTemplateFromJavaFile(stereotype + ".vm"), velocityContext));
    }

    public void writeAngularFile(String sufficeFile, String stereotype, VelocityContext velocityContext){

        String basePath = getBaseAngularPath();

        String composant = transformToHyphens(className, "-", false);
        String sep = ".";
        if (sufficeFile.startsWith("list") || sufficeFile.startsWith("form")){
            sep = "-";
        }
        if (sufficeFile.startsWith("list")){
            composant = composant + "-list";
        }
        if (sufficeFile.startsWith("form")){
            composant = composant + "-form";
        }

        Path path = Paths.get(basePath + "/" + stereotype + "/" + ssPkgFonctionnel.replace(".", "/") + "/" +  composant + "/" + transformToHyphens(className, "-", false) + sep + sufficeFile);
        writeFile(path, fillVelocityTemplate(getContentVelocityTemplateFromAngularFile(sufficeFile + ".vm"), velocityContext));
    }

    private static String transformToHyphens(String input, String sep, boolean pluriel) {
        // Step 1: Convert camel case to hyphenated
        String hyphenated = input.replaceAll("([a-z])([A-Z])", String.format("$1%s$2", sep)).toLowerCase();

        // Step 2: Split the string to handle pluralization
        String[] parts = hyphenated.split("-");

        String end = pluriel ? "s" : "";
        // Step 3: Pluralize the first word
        if (parts.length > 0) {
            parts[0] = parts[0] + end;
        }

        // Step 4: Join the parts back together
        return String.join("-", parts);
    }
}
