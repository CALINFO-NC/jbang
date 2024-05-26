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
import java.util.Objects;

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

        generateJavaEntity();
        generateJavaRepository();
        generateJavaService();
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

    private String angularProperties(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                String angularType = convertToAngularType(variableTree.getType().toString());

                if (angularType != null) {
                    result.append("\n  " + variableTree.getName().toString() + ": " + angularType + ";");
                }
            }

            if (variableTree.getType().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree) variableTree.getType();

                boolean collection = Objects.equals(parameterizedTypeTree.getType().toString(), "List") ||
                        Objects.equals(parameterizedTypeTree.getType().toString(), "Collection");

                if (collection && parameterizedTypeTree.getTypeArguments().size() == 1){

                    Tree tree = parameterizedTypeTree.getTypeArguments().get(0);

                    if (tree.getKind() == Tree.Kind.IDENTIFIER){
                        IdentifierTree identifierTree = (IdentifierTree)tree;
                        String angularType = convertToAngularType(identifierTree.getName().toString());

                        if (angularType != null) {
                            result.append("\n  " + variableTree.getName().toString() + ": " + angularType + "[] = [];");
                        }
                    }
                }
            }
        }

        return result.toString();
    }

    private String htmlTableHeader(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                String angularType = convertToAngularType(variableTree.getType().toString());

                if (angularType != null) {

                    result.append("\n");
                    result.append(String.format("""
                               <th pSortableColumn="%s">
                               <div class="flex justify-content-between align-items-center">
                                 %s
                                 <p-sortIcon field="%s"></p-sortIcon>
                                 <p-columnFilter [matchModeOptions]="TableFilterHelper.getDefaultMatchModesFor%s()"
                                                 class="ml-auto" display="menu"
                                                 field="%s"
                                                 type="%s"></p-columnFilter>
                               </div>
                             </th>
                            """,
                            variableTree.getName().toString(),
                            StringUtils.capitalize(variableTree.getName().toString()),
                            variableTree.getName().toString(),
                            StringUtils.capitalize(angularType),
                            variableTree.getName().toString(),
                            angularType.equals("string") ? "text" : angularType));
                }
            }
        }

        return result.toString();
    }

    private String htmlTableData(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                String angularType = convertToAngularType(variableTree.getType().toString());

                if (angularType != null) {
                    result.append("\n");
                    result.append(String.format("""
                              <td>
                                <span class="p-column-title">%s</span>
                                {{ %s.%s }}
                              </td>
                            """,
                            StringUtils.capitalize(variableTree.getName().toString()),
                            StringUtils.uncapitalize(className),
                            variableTree.getName().toString()));
                }
            }
        }

        return result.toString();
    }

    private String htmlFormData(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                String angularType = convertToAngularType(variableTree.getType().toString());

                if (angularType != null) {
                    result.append("\n");
                    result.append(String.format("""
                            <label class="col-12 md:col-2 required" for="%s">%s</label>
                            <div class="col-12">
                              <input formControlName="%s" id="%s" pInputText type="%s">
                              <div *ngIf="%sControl.invalid && %sControl.touched">
                                <small *ngIf="%sControl.errors?.required" class="text-danger">{{ ERR_CHAMP_OBLIGATOIRE }}</small>
                              </div>
                            </div>
                            """,
                            variableTree.getName().toString(),
                            StringUtils.capitalize(variableTree.getName().toString()),
                            variableTree.getName().toString(),
                            variableTree.getName().toString(),
                            angularType.equals("string") ? "text" : angularType,
                            variableTree.getName().toString(),
                            variableTree.getName().toString(),
                            variableTree.getName().toString()));
                }
            }
        }

        return result.toString();
    }

    private String formBuilderDecl(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                String angularType = convertToAngularType(variableTree.getType().toString());

                if (angularType != null) {
                    result.append(String.format("""
                              %s: [null, Validators.required],
                            """,
                            variableTree.getName().toString()));
                }
            }
        }

        return result.toString();
    }

    private String formBuilderUpdate(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                String angularType = convertToAngularType(variableTree.getType().toString());

                if (angularType != null) {
                    result.append(String.format("""
                              %s: this.loaded%s.%s,
                            """,
                            variableTree.getName().toString(),
                            className,
                            variableTree.getName().toString()));
                }
            }
        }

        return result.toString();
    }

    private String formControl(){

        StringBuilder result = new StringBuilder();

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}");

        String baseApiPath = templateParam.get("baseApiPath");

        for (VariableTree variableTree: ClassPropertiesUtils.getPropetiesFromJavaClassFile(new File(baseApiPath + "Resource.java"))){
            if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                String angularType = convertToAngularType(variableTree.getType().toString());

                if (angularType != null) {
                    result.append(String.format("""
                            get %sControl(): FormControl {
                              return this.%sForm.get('%s') as FormControl;
                            }
                            """,
                            variableTree.getName().toString(),
                            StringUtils.uncapitalize(className),
                            variableTree.getName().toString()));
                }
            }
        }

        return result.toString();
    }

    private String convertToAngularType(String javaType){

        if (Objects.equals("String", javaType)){
            return "string";
        }

        if (Objects.equals("Integer", javaType) ||
                Objects.equals("Long", javaType) ||
                Objects.equals("BigDecimal", javaType) ||
                Objects.equals("Float", javaType) ||
                Objects.equals("Double", javaType) ||
                Objects.equals("float", javaType) ||
                Objects.equals("double", javaType) ||
                Objects.equals("int", javaType)){
            return "number";
        }

        if (Objects.equals("Boolean", javaType) ||
                Objects.equals("boolean", javaType)){
            return "boolean";
        }

        return null;
    }

    public void generateJavaEntity(){

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

    public void generateJavaController(){


        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.controller");
        templateParam.put("classNameUrl", transformToHyphens(className, true));


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

    public void generateJavaServiceImpl(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "com.calinfo.api.${projectNameMin}.metier.${ssPkgFonctionnel}.service.impl");
        templateParam.put("classNameMin", className.toLowerCase());
        templateParam.put("variableName", StringUtils.uncapitalize(className));

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

    public void generateJavaService(){


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

    public void generateJavaRepository(){

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

    public void generateJavaConverter(){

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

    public void generateAngularModel(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("classNameMin", className.toLowerCase());
        templateParam.put("properties", angularProperties());

        String template = """
                 import {AbstractModel} from "@core/abstract.model";
                 
                 export class ${className}Model extends AbstractModel<number> {
                    ${properties}
                 }
                """;

        Path path = Paths.get(fillTemplate("${baseAngularPath}/services/" + ssPkgFonctionnel.replace(".", "/") + "/" + transformToHyphens(className, false) + ".model.ts", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateAngularService(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("classNameUrl", transformToHyphens(className, true));
        templateParam.put("classNameMin", transformToHyphens(className, false));
        templateParam.put("ssPathFonctionnel", ssPkgFonctionnel.replace(".", "/"));

        String template = """
                 
                 import {Injectable} from "@angular/core";
                 import {Observable} from "rxjs";
                 import {HttpClient} from "@angular/common/http";
                 import {plainToClass} from "class-transformer";
                 import {map, tap} from "rxjs/operators";
                 import {CryptoModel} from "@services/${ssPathFonctionnel}/${classNameMin}.model";
                 import {environment} from "@env/environment";
                 import {ListDto} from "@core/list-dto";
                 import {HttpQueryUtils} from "@core/utils/http-query.utils";
                 
                 @Injectable({
                   providedIn: 'root'
                 })
                 export class ${className}Service {
                 
                   private readonly baseUrl = `${environment.apiPrefixUrl}/${classNameUrl}`;
                 
                   protected constructor(protected httpClient: HttpClient) {
                   }
                 
                 
                   update(id: number, inputData: any) {
                     const url = `${this.baseUrl}/${id}`;
                 
                     return this.httpClient.put(url, inputData, environment.httpOptions).pipe(
                       map(response => plainToClass(${className}Model, response)),
                     );
                   }
                 
                   save(inputData: any) {
                 
                     return this.httpClient.post(this.baseUrl, inputData, environment.httpOptions).pipe(
                       map(response => plainToClass(${className}Model, response)),
                     );
                   }
                 
                   read(id: number): Observable<${className}Model> {
                     const url = `${this.baseUrl}/${id}`;
                 
                     return this.httpClient.get(url, environment.httpOptions).pipe(
                       map(response => plainToClass(${className}Model, response)),
                     );
                   }
                 
                   list(first: number,
                        limit: number,
                        filters?: { [s: string]: any },
                        sortField?: string | string[], sortOrder?: number,
                        customBaseUrl?: string)
                     : Observable<ListDto<${className}Model>> {
                 
                     let url = this.baseUrl;
                     if (customBaseUrl)
                       url = customBaseUrl;
                 
                     const queryParams = HttpQueryUtils.buildListQueryParams(first, limit, filters, sortField, sortOrder);
                     if (queryParams) url += `?${queryParams}`;
                 
                     return this.httpClient.get<ListDto<${className}Model>>(url, environment.httpOptions).pipe(
                       tap(response => response.data = response.data.map(item => plainToClass(${className}Model, item))),
                     );
                   }
                 
                   delete(id: number, customBaseUrl?: string): Observable<void> {
                 
                     let url = this.baseUrl;
                     if (customBaseUrl)
                       url = customBaseUrl;
                 
                     return this.httpClient.delete<void>(`${url}/${id}`, environment.httpOptions);
                   }
                 
                 }
                 
                 
                """;

        Path path = Paths.get(fillTemplate("${baseAngularPath}/services/${ssPathFonctionnel}/" + transformToHyphens(className, false) + ".service.ts", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateAngularListTs(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("classNameUrl", transformToHyphens(className, true));
        templateParam.put("classNameMin", transformToHyphens(className, false));
        templateParam.put("variableName", StringUtils.uncapitalize(className));
        templateParam.put("ssPathFonctionnel", ssPkgFonctionnel.replace(".", "/"));

        String template = """
                 
                 import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
                 import {Table, TableLazyLoadEvent} from "primeng/table";
                 import {AppConfig} from "@app/app.config";
                 import {ConfirmationService, MessageService, TableState} from "primeng/api";
                 import {TableFilterHelper} from "@core/utils/table-filter.helper";
                 import {ActivatedRoute, Router} from "@angular/router";
                 import {${className}Service} from "@app/services/${ssPathFonctionnel}/${classNameMin}.service";
                 import {${className}Model} from "@app/services/${ssPathFonctionnel}/${classNameMin}.model";
                 
                 @Component({
                   selector: 'app-${classNameMin}-list',
                   templateUrl: './${classNameMin}-list.component.html',
                   styleUrls: ['./${classNameMin}-list.component.scss']
                 })
                 export class ${className}ListComponent implements AfterViewInit {
                 
                   @ViewChild('globalSearch') globalSearch: ElementRef;
                   globalSearchToRestore: string;
                   defaultRowsPerPage = AppConfig.defaultRowsPerPage;
                   loading = false;
                   totalRecords: number;
                   data: any[];
                   protected readonly TableFilterHelper = TableFilterHelper;
                   private forwardedMessage: any;
                   private lastLoadEvent: string;
                 
                   constructor(private ${variableName}Service: ${className}Service,
                               private messageService: MessageService,
                               private router: Router,
                               private confirmationService: ConfirmationService,
                               private route: ActivatedRoute,
                   ) {
                     this.forwardedMessage = this.router.getCurrentNavigation()?.extras?.state?.forwardedMessage;
                   }
                 
                   ngAfterViewInit(): void {
                     if (this.forwardedMessage) {
                       this.messageService.add(this.forwardedMessage);
                       this.forwardedMessage = null;
                     }
                     this.globalSearch.nativeElement.value = this.globalSearchToRestore ?? "";
                   }
                 
                   loadData(event: TableLazyLoadEvent) {
                     this.lastLoadEvent = JSON.stringify(event);
                 
                     const filters = event.filters ?? {};
                 
                     const req = this.${variableName}Service.list(event.first, event.rows ? event.rows : this.defaultRowsPerPage, filters, event.sortField, event.sortOrder);
                 
                     this.loading = true;
                     req.subscribe({
                       next: value => {
                         this.totalRecords = value.count;
                         this.data = value.data;
                         this.loading = false;
                       }, error: () => {
                         this.loading = false;
                       }
                     });
                   }
                 
                   goTo${className}(${variableName}: ${className}Model) {
                     this.router.navigate(['${variableName}s', ${variableName}.id]);
                   }
                 
                   onDelete(event: Event, ${variableName}: ${className}Model) {
                 
                     this.confirmationService.confirm({
                       target: event.target || undefined,
                       key: "confirmPopup",
                       message: "Êtes-vous sûr de vouloir effectuer cette suppression ?",
                       icon: 'pi pi-exclamation-triangle',
                       acceptLabel: "Supprimer",
                       acceptButtonStyleClass: "p-button-danger",
                       rejectLabel: "Annuler",
                       rejectButtonStyleClass: "p-button-outlined",
                       accept: () => {
                 
                         this.${variableName}Service.delete(${variableName}.id).subscribe(() => {
                           this.reload();
                         });
                       },
                     });
                   }
                 
                   handleStateRestore(tableState: TableState) {
                     this.globalSearchToRestore = (tableState?.filters?.global as any)?.value;
                   }
                 
                   clearFilters(dt: Table) {
                     this.globalSearch.nativeElement.value = "";
                     delete dt.filters['global'];
                     dt.clear();
                   }
                 
                   /**
                    * Recharge la grille sans changer les filtres/tries
                    */
                   reload() {
                     if (this.lastLoadEvent != null) {
                       const loadEvent = JSON.parse(this.lastLoadEvent);
                       this.lastLoadEvent = null;
                       this.loadData(loadEvent);
                     }
                   }
                 
                 }
                """;

        Path path = Paths.get(fillTemplate("${baseAngularPath}/views/${ssPathFonctionnel}/" + transformToHyphens(className, false) + "-list/" + transformToHyphens(className, false) + "-list.component.ts", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateAngularListHtml(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("classNameUrl", transformToHyphens(className, true));
        templateParam.put("classNameMin", transformToHyphens(className, false));
        templateParam.put("variableName", StringUtils.uncapitalize(className));
        templateParam.put("ssPathFonctionnel", ssPkgFonctionnel.replace(".", "/"));
        templateParam.put("htmlHeader", htmlTableHeader());
        templateParam.put("htmlData", htmlTableData());

        String template = """
                 
                 <h3><i class="fa-solid fa-clipboard-question"></i> Votre FAQ</h3>
                  <p-table #dt (onLazyLoad)="loadData($event)" (onRowSelect)="goTo${className}($event.data); dt.selection = null"
                           (onStateRestore)="handleStateRestore($event)"
                           [filterDelay]="200" [lazy]="true" [loading]="loading" [paginator]="true"
                           [rowsPerPageOptions]="[5,10,20,50]"
                           [rows]="defaultRowsPerPage" [showCurrentPageReport]="true" [sortOrder]="1" [totalRecords]="totalRecords"
                           [value]="data" currentPageReportTemplate="Affichage des lignes {first} à {last} sur {totalRecords}"
                           dataKey="id"
                           selectionMode="single" sortField="question"
                           sortMode="single"
                           stateKey="${variableName}list-state" stateStorage="session" styleClass="p-datatable-striped p-datatable-responsive">
                    <ng-template pTemplate="caption">
                      <div class="flex justify-content-start">
                        <span class="p-input-icon-left">
                          <i class="pi pi-search"></i>
                            <input #globalSearch (input)="dt.filterGlobal($event.target.value, undefined)" pInputText
                                   placeholder="Recherche textuelle..."
                                   type="text"/>
                          </span>
                        <p-button (onClick)="clearFilters(dt)" icon="pi pi-filter-slash" label="Vider les filtres"
                                  styleClass="mx-1 p-button-outlined"></p-button>
                        <div class="ml-auto"></div>
                  
                        <p-button [preserveFragment]="true" icon="pi pi-plus" label="Créer une question" routerLink="./new"
                                  styleClass="mr-1"></p-button>
                      </div>
                    </ng-template>
                  
                    <ng-template pTemplate="header">
                      <tr>
                        ${htmlHeader}
                        <th style="width: 3rem"></th>
                      </tr>
                    </ng-template>
                    <ng-template let-${variableName} pTemplate="body">
                      <tr [pSelectableRow]="${variableName}">
                        ${htmlData}
                        <td>
                          <div class="button-action-column button-action-column-xs">
                            <button (click)="onDelete($event, ${variableName})" appendTo="body" icon="fa-solid fa-trash-can"
                                    pTooltip="Supprimer" class="p-button-danger"
                                    tooltipPosition="bottom" pButton></button>
                          </div>
                  
                        </td>
                      </tr>
                    </ng-template>
                  
                  </p-table>
                  <p-menu #menu [popup]="true"></p-menu>
                  
                """;

        Path path = Paths.get(fillTemplate("${baseAngularPath}/views/${ssPathFonctionnel}/" + transformToHyphens(className, false) + "-list/" + transformToHyphens(className, false) + "-list.component.html", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateAngularListScss(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("ssPathFonctionnel", ssPkgFonctionnel.replace(".", "/"));

        String template = "";

        Path path = Paths.get(fillTemplate("${baseAngularPath}/views/${ssPathFonctionnel}/" + transformToHyphens(className, false) + "-list/" + transformToHyphens(className, false) + "-list.component.scss", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateAngularFormTs(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("classNameUrl", transformToHyphens(className, true));
        templateParam.put("classNameMin", transformToHyphens(className, false));
        templateParam.put("variableName", StringUtils.uncapitalize(className));
        templateParam.put("ssPathFonctionnel", ssPkgFonctionnel.replace(".", "/"));
        templateParam.put("formBuilderDecl", formBuilderDecl());
        templateParam.put("formBuilderUpdate", formBuilderUpdate());
        templateParam.put("formControl", formControl());

        String template = """
                 
                 import {Component, OnInit} from '@angular/core';
                 import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
                 import {AppConfig, MessageCode} from "@app/app.config";
                 import {Observable} from "rxjs";
                 import {ActivatedRoute, Router} from "@angular/router";
                 import {ConfirmBeforeDeactivateComponent} from "@core/confirm-before-deactivate-guard";
                 import {ConfirmationService, MessageService} from "primeng/api";
                 import {${className}Service} from "@app/services/${ssPathFonctionnel}/${classNameMin}.service";
                 import {${className}Model} from "@app/services/${ssPathFonctionnel}/${classNameMin}.model";
                 
                 @Component({
                   selector: 'app-${variableName}-form',
                   templateUrl: './${variableName}-form.component.html',
                   styleUrls: ['./${variableName}-form.component.scss']
                 })
                 export class ${className}FormComponent implements OnInit, ConfirmBeforeDeactivateComponent {
                   ERR_CHAMP_OBLIGATOIRE = AppConfig.getMessage(MessageCode.ERR_CHAMP_OBLIGATOIRE);
                   ${variableName}Form: FormGroup;
                   loaded${className}: ${className}Model;
                   forceExit = false;
                   processing = false;
                   private readonly currentYear = new Date().getFullYear();
                 
                   constructor(private ${variableName}Service: ${className}Service,
                               private fb: FormBuilder,
                               private route: ActivatedRoute,
                               private router: Router,
                               protected confirmationService: ConfirmationService,
                               protected messageService: MessageService,
                   ) {
                 
                     this.${variableName}Form = this.fb.group({
                       ${formBuilderDecl}
                     });
                   }
                 
                   ${formControl}
                 
                   ngOnInit() {
                     this.route.data.subscribe(value => {
                       this.loaded${className} = value['${variableName}'];
                       if (this.loaded${className})
                         this.updateView();
                     });
                   }
                 
                   updateView() {
                     this.${variableName}Form.patchValue({
                       ${formBuilderUpdate}
                     });
                     this.${variableName}Form.markAsPristine();
                   }
                 
                   goBack(event: MouseEvent) {
                     if (this.${variableName}Form.dirty) {
                       this.confirmationService.confirm({
                         target: event.target,
                         key: "confirmPopup",
                         message: this.getConfirmMessage(),
                         icon: 'pi pi-exclamation-triangle',
                         acceptLabel: "Confirmer",
                         rejectLabel: "Annuler",
                         accept: () => {
                           this.forceExit = true;
                           this.router.navigate([`/${variableName}s`]);
                         }
                       });
                     } else {
                       this.router.navigate([`/${variableName}s`]);
                     }
                   }
                 
                   // validation custom du formulaire par rapport aux pièces jointes
                   isFormValid() {
                     return this.${variableName}Form.valid;
                   }
                 
                   save(exit?: boolean) {
                     if (this.isFormValid()) {
                 
                       const formData = this.${variableName}Form.value;
                 
                       let saveReq: Observable<${className}Model>;
                       if (this.loaded${className}) {
                         formData.id = this.loaded${className}.id;
                         saveReq = this.${variableName}Service.update(this.loaded${className}.id, formData);
                       } else {
                         saveReq = this.${variableName}Service.save(formData);
                       }
                 
                       this.processing = true;
                       saveReq.subscribe({
                         next: result => {
                           this.afterSaveSucceed(result, exit);
                         }, error: () => {
                           this.processing = false;
                           if (this.loaded${className})
                             this.updateView();
                         }
                       });
                 
                     } else {
                       this.messageService.add({severity: 'error', summary: 'Le formulaire est invalide'});
                       this.${variableName}Form.markAllAsTouched();
                     }
                   }
                 
                   valider() {
                     this.messageService.add({severity: 'info', detail: 'Pas encore implémenté'});
                   }
                 
                   rejeter() {
                     this.messageService.add({severity: 'info', detail: 'Pas encore implémenté'});
                   }
                 
                   mettreEnAttente() {
                     this.messageService.add({severity: 'info', detail: 'Pas encore implémenté'});
                   }
                 
                   getConfirmMessage(): string {
                     return AppConfig.getMessage(this.loaded${className} ? MessageCode.MSG_CONFIRM_CANCEL_EDIT : MessageCode.MSG_CONFIRM_CANCEL_CREATE, ['de la ${variableName}']);
                   }
                 
                   isConfirmNeeded(): boolean {
                     return !this.forceExit && this.${variableName}Form.dirty;
                   }
                 
                   private afterSaveSucceed(savedModel: ${className}Model, exit: boolean) {
                     this.processing = false;
                     const successMessage = AppConfig.getMessage(MessageCode.MSG_SAVE, ["La ${variableName}"]);
                 
                     if (exit) {
                       this.forceExit = true;
                       this.router.navigate(["/${variableName}s"], {
                         state: {
                           forwardedMessage: {
                             severity: 'success',
                             summary: successMessage,
                           }
                         }
                       });
                     } else {
                       if (!this.loaded${className})
                         window.history.pushState({}, "", `/${variableName}s/${savedModel.id}`);
                       this.loaded${className} = savedModel;
                       this.messageService.add({severity: 'success', summary: successMessage});
                       this.updateView();
                     }
                   }
                 }
                 
                """;

        Path path = Paths.get(fillTemplate("${baseAngularPath}/views/${ssPathFonctionnel}/" + transformToHyphens(className, false) + "-form/" + transformToHyphens(className, false) + "-form.component.ts", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateAngularFormHtml(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("variableName", StringUtils.uncapitalize(className));
        templateParam.put("htmlFormData", htmlFormData());
        templateParam.put("ssPathFonctionnel", ssPkgFonctionnel.replace(".", "/"));

        String template = """
                <form [formGroup]="${variableName}Form">
                                
                                
                  <div class="flex mb-2 justify-content-between">
                    <div class="flex">
                      <p-button (onClick)="goBack($event)" class="mr-1" icon="pi pi-angle-left" label="Retour à la liste"
                                styleClass="p-button-outlined p-button-secondary responsive-button-priority-1"></p-button>
                      <p-button (onClick)="save()" [disabled]="processing" class="mr-1" icon="pi pi-save" label="Enregistrer"
                                styleClass="responsive-button-priority-3"></p-button>
                      <p-button (onClick)="save(true)" [disabled]="processing" class="mr-1" icon="fas fa-door-open"
                                label="Enregistrer et fermer"></p-button>
                    </div>
                                
                  </div>
                                
                  <div class="card p-fluid">        
                    <div class="field grid">
                      ${htmlFormData}
                    </div>         
                  </div>
                                
                </form>                 
                """;

        Path path = Paths.get(fillTemplate("${baseAngularPath}/views/${ssPathFonctionnel}/" + transformToHyphens(className, false) + "-form/" + transformToHyphens(className, false) + "-form.component.html", templateParam));
        writeFile(path, fillTemplate(template, templateParam));
    }

    public void generateAngularFormScss(){

        Map<String, String> templateParam = new HashMap<>();
        fillDefaultParam(templateParam, "");
        templateParam.put("ssPathFonctionnel", ssPkgFonctionnel.replace(".", "/"));

        String template = """               
                """;

        Path path = Paths.get(fillTemplate("${baseAngularPath}/views/${ssPathFonctionnel}/" + transformToHyphens(className, false) + "-form/" + transformToHyphens(className, false) + "-form.component.scss", templateParam));
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
        templateParam.put("baseAngularPath", projectPath + "/" + projectName.toLowerCase(Locale.ROOT) + "/src/main/angular/" + projectName.toLowerCase(Locale.ROOT) + "/src/app/");
        templateParam.put("pkg", pkg);
    }

    private static String transformToHyphens(String input, boolean pluriel) {
        // Step 1: Convert camel case to hyphenated
        String hyphenated = input.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();

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
