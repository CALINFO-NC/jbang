package scripts.jbang.service;

import com.sun.source.tree.*;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import picocli.CommandLine;
import scripts.jbang.Console;
import scripts.jbang.ScriptCallable;
import scripts.jbang.ScriptCommand;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

        generateController();
        generateEntity();
        generateService();
        generateServiceImpl();
        generateRepository();
        generateConverter();
    }

    private String properties(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                result.append("\n\n     private " + variableTree.getType().toString() + " " + variableTree.getName() + ";");
            }
        }

        return result.toString();
    }

    private String convertCode(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                result.append("\n        dest.set" + StringUtils.capitalize(variableTree.getName().toString()) + "(source.get" + StringUtils.capitalize(variableTree.getName().toString()) + "());");
            }
        }

        return result.toString();
    }

    public void generateEntity(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.entity.${tenant}");
        templateParam.put("classNameMin", className.toLowerCase());
        templateParam.put("properties", properties());

        String template = """
                 package ${pkg};
                 
                 import com.calinfo.api.${projectNameMin}.core.entity.AbstractEntity;
                 import jakarta.persistence.*;
                 import lombok.Getter;
                 import lombok.Setter;
                 
                 @Getter
                 @Setter
                 @Entity
                 @Table(name = "${tenant}_${classNameMin}")
                 public class ${className}Entity extends AbstractEntity {
                     ${properties}
                 }
                """;

        Path path = Paths.get(fillTemplate("${basePath}Entity.java", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateController(){


        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.controller");
        templateParam.put("classNameUrl", transformToPluralsAndHyphens(className));


        String template = """
                package ${pkg};
                
                import com.calinfo.api.${projectNameMin}.core.controller.*;
                import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.${className}Resource;
                import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.${className}Service;
                import io.swagger.v3.oas.annotations.tags.Tag;
                import lombok.experimental.Delegate;
                import lombok.extern.slf4j.Slf4j;
                import org.springframework.web.bind.annotation.RequestMapping;
                import org.springframework.web.bind.annotation.RestController;
                
                @Slf4j
                @RequestMapping({
                        "$${${projectNameMin}-api.baseApiUrl:}/api/v1/private/${classNameUrl}"
                })
                @RestController
                @Tag(name = "${className}", description = "<div class='open-api-tag-description'/>")
                public class ${className}Controller implements
                        CreateController<${className}Resource>,
                        UpdateController<${className}Resource, Long>,
                        DeleteController<Long>,
                        ReadController<${className}Resource, Long>,
                        ListController<${className}Resource>,
                        LookupController<${className}Resource> {
                                
                    @Delegate
                    private final CreateController<CryptoResource> createController;
                               
                    @Delegate
                    private final UpdateController<CryptoResource, Long> updateController;
                                
                    @Delegate
                    private final DeleteController<Long> deleteController;
                                
                    @Delegate
                    private final ListController<${className}Resource> listController;
                                
                    @Delegate
                    private final ReadController<${className}Resource, Long> readController;
                                
                    @Delegate
                    private final LookupController<${className}Resource> lookupController;
                                
                    public ${className}Controller(${className}Service service) {
                        this.createController = new DefaultCreateController<>(service);
                        this.updateController = new DefaultUpdateController<>(service);
                        this.deleteController = new DefaultDeleteController<>(service);
                        this.readController = new DefaultReadController<>(service);
                        this.listController = new DefaultListController<>(service);
                        this.lookupController = new DefaultLookupController<>(service);
                    }         
                }
                """;



        Path path = Paths.get(fillTemplate("${basePath}Controller.java", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateServiceImpl(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.service.impl");
        templateParam.put("classNameMin", className.toLowerCase());
        templateParam.put("variableName", StringUtils.capitalize(className));

        String template = """
              package ${pkg};
              
              import com.calinfo.api.${projectNameMin}.core.dto.query.FullText;
              import com.calinfo.api.${projectNameMin}.core.entity.RsqlSpecification;
              import com.calinfo.api.${projectNameMin}.core.utils.PaginationUtils;
              import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.${className}Resource;
              import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.${className}Service;
              import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.entity.${tenant}.${className}Entity;
              import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.repository.${tenant}.${className}Repository;
              import com.calinfo.api.common.converter.AbstractConvertManager;
              import com.calinfo.api.common.domain.DomainDatasourceConfiguration;
              import com.calinfo.api.common.dto.DynamicListDto;
              import com.calinfo.api.common.dto.MultiSorter;
              import com.calinfo.api.common.dto.PageInfoDto;
              import com.calinfo.api.common.spring.PageInfoPageRequest;
              import jakarta.validation.Valid;
              import jakarta.validation.constraints.NotNull;
              import lombok.RequiredArgsConstructor;
              import org.springframework.data.domain.Page;
              import org.springframework.stereotype.Service;
              import org.springframework.transaction.annotation.Propagation;
              import org.springframework.transaction.annotation.Transactional;
              
              
              @RequiredArgsConstructor
              @Service
              @Transactional(propagation = Propagation.REQUIRED, transactionManager = DomainDatasourceConfiguration.TRANSACTION_MANAGER_REF)
              public class ${className}ServiceImpl implements ${className}Service {
              
                  private final AbstractConvertManager convertManager;
                  private final ${className}Repository ${variableName}Repository;
              
                  @Override
                  public ${className}Resource create(@NotNull @Valid ${className}Resource ${variableName}Resource) {
              
                      ${className}Entity ${variableName}Entity = convertManager.convert(${variableName}Resource, new ${className}Entity());
                      ${variableName}Entity = ${variableName}Repository.save(${variableName}Entity);
              
                      return read(${variableName}Entity.getId());
                  }
              
              
                  @Override
                  public ${className}Resource read(@NotNull Long id) {
              
                      ${className}Entity ${variableName}Entity = ${variableName}Repository.findById(id).orElseThrow();
              
                      return convertManager.convert(${variableName}Entity, new ${className}Resource());
                  }
              
                  @Override
                  public ${className}Resource update(@NotNull Long id, @NotNull @Valid ${className}Resource ${variableName}Resource) {
              
                      ${className}Entity ${variableName}Entity = ${variableName}Repository.findById(id).orElseThrow();
                      ${variableName}Entity = convertManager.convert(${variableName}Resource, ${variableName}Entity);
                      ${variableName}Entity = ${variableName}Repository.save(${variableName}Entity);
              
                      return read(${variableName}Entity.getId());
                  }
              
                  @Override
                  public void delete(@NotNull Long id) {
              
                      ${className}Entity ${variableName}Entity = ${variableName}Repository.findById(id).orElseThrow();
                      ${variableName}Repository.delete(${variableName}Entity);
                  }
              
                  @Override
                  public DynamicListDto<${className}Resource> list(@NotNull PageInfoDto pageInfoDto, @NotNull MultiSorter multiSorter, String query) {
              
                      PageInfoDto ci = PaginationUtils.setMaxPaginationItem(pageInfoDto);
                      PageInfoPageRequest pageRequest = new PageInfoPageRequest(ci, PaginationUtils.convertSortParam(multiSorter));
                      RsqlSpecification<${className}Entity> rsqlSpecification = new RsqlSpecification<>(query, ${className}Entity.class, convertManager);
                      Page<${className}Entity> page = ${variableName}Repository.findAll(rsqlSpecification, pageRequest);
              
                      return new DynamicListDto<>(convertManager.toFunction(${className}Resource.class), page);
              
                  }
              
                  @Override
                  public DynamicListDto<${className}Resource> lookup(@NotNull PageInfoDto pageInfoDto, @NotNull MultiSorter multiSorter, String fulltextQuery) {
              
                      FullText fullText = new FullText(${className}Resource.class, fulltextQuery);
                      String query = convertManager.convert(fullText, String.class);
              
                      return list(pageInfoDto, multiSorter, query);
                  }
              }
              """;


        Path path = Paths.get(fillTemplate("${basePath}ServiceImpl.java", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateService(){


        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");


        String template = """
                package ${pkg};
                
                import com.calinfo.api.${projectNameMin}.core.service.CrudService;
                import com.calinfo.api.common.service.ListService;
                import com.calinfo.api.common.service.LookupService;
                import org.springframework.validation.annotation.Validated;
                 
                @Validated
                public interface ${className}Service extends CrudService<${className}Resource, Long>, ListService<${className}Resource>, LookupService<${className}Resource> {
                }
                """;


        Path path = Paths.get(fillTemplate("${baseApiPath}Service.java", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateRepository(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.repository.${tenant}");


        String template = """
                package ${pkg};
                
                import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.entity.${tenant}.${className}Entity;
                import org.springframework.data.jpa.repository.JpaRepository;
                import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
                 
                public interface ${className}Repository extends JpaRepository<${className}Entity, Long>, JpaSpecificationExecutor<${className}Entity> {
                }
                """;


        Path path = Paths.get(fillTemplate("${basePath}Repository.java", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateConverter(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.converter");
        templateParam.put("convertCode", convertCode());


        String template = """
                package ${pkg};
                                
                import com.calinfo.api.${projectNameMin}.core.converter.BiDirectionelConverter;
                import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.${className}Resource;
                import com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.entity.${tenant}.${className}Entity;
                                
                public class ${className}EntityResourceConverter extends BiDirectionelConverter<${className}Entity, ${className}Resource> {
                                
                    public ${className}EntityResourceConverter() {
                        super(${className}Entity.class, ${className}Resource.class);
                    }
                                
                    @Override
                    protected ${className}Entity convertToE(${className}Resource source, ${className}Entity dest) {
                        ${convertCode}
                                
                        return dest;
                    }
                                
                    @Override
                    protected ${className}Resource convertToR(${className}Entity source, ${className}Resource dest) {
                        ${convertCode}
                                
                        return dest;
                    }
                }
                                
                """;


        Path path = Paths.get(fillTemplate("${basePath}EntityResourceConverter.java", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    private String fillTemplate(String template, Map<String, String> templateParam){

        StringSubstitutor sub = new StringSubstitutor(templateParam);
        return sub.replace(template);

    }

    @SneakyThrows
    private void writeFile(Path path, String content){

        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes());
    }

    private void fillDefaultParam(Map<String, String> templateParam, String templatePakage){

        templateParam.put("projectNameMin", projectName.toLowerCase());
        templateParam.put("ssPkgFonctionnel", ssPkgFonctionnel);
        templateParam.put("className", className);
        templateParam.put("tenant", tenant);

        String pkg = fillTemplate(templatePakage, templateParam);

        templateParam.put("basePath", projectPath + "/" + projectName.toLowerCase(Locale.ROOT) + "/src/main/java/" + pkg.replace(".", "/") + "/" + className);
        templateParam.put("baseApiPath", projectPath + "/" + projectName.toLowerCase(Locale.ROOT) + "-api/src/main/java/" + pkg.replace(".", "/") + "/" + className);
        templateParam.put("pkg", pkg);
    }

    private static String transformToPluralsAndHyphens(String input) {
        // Step 1: Convert camel case to hyphenated
        String hyphenated = input.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();

        // Step 2: Split the string to handle pluralization
        String[] parts = hyphenated.split("-");

        // Step 3: Pluralize the first word
        if (parts.length > 0) {
            parts[0] = parts[0] + "s";
        }

        // Step 4: Join the parts back together
        return String.join("-", parts);
    }
}
