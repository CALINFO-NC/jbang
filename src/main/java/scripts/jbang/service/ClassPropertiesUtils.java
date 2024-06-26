package scripts.jbang.service;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SimpleTreeVisitor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassPropertiesUtils {

    @Deprecated
    @SneakyThrows
    public static List<VariableTree> getPropetiesFromJavaClassFile(File javaFile){

        ArrayList<VariableTree> result = new ArrayList<>();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile));

        JavacTask javacTask =
                (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();

        CompilationUnitTree compilationUnitTree = compilationUnitTrees.iterator().next();

        for (Tree tree : compilationUnitTree.getTypeDecls()) {
            if (tree.getKind() == Tree.Kind.CLASS) {
                ClassTree classTree = (ClassTree) tree;
                for (Tree member : classTree.getMembers()) {
                    if (member.getKind() == Tree.Kind.VARIABLE) {
                        VariableTree variableTree = (VariableTree)member;
                        result.add(variableTree);
                    }
                }
            }
        }

        return result;
    }

    @SneakyThrows
    public static List<FieldProperty> getFieldsFromJavaClassFile(File javaFile){

        ArrayList<FieldProperty> result = new ArrayList<>();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile));

        JavacTask javacTask =
                (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();

        CompilationUnitTree compilationUnitTree = compilationUnitTrees.iterator().next();

        for (Tree tree : compilationUnitTree.getTypeDecls()) {
            if (tree.getKind() == Tree.Kind.CLASS) {
                ClassTree classTree = (ClassTree) tree;
                for (Tree member : classTree.getMembers()) {
                    if (member.getKind() == Tree.Kind.VARIABLE) {
                        VariableTree variableTree = (VariableTree)member;

                        FieldProperty field = new FieldProperty();
                        field.setName(variableTree.getName().toString());

                        if (variableTree.getType().getKind() == Tree.Kind.IDENTIFIER) {
                            field.setType(variableTree.getType().toString());
                        }

                        if (variableTree.getType().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                            ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree) variableTree.getType();

                            parameterizedTypeTree.getTypeArguments().forEach(treeArg -> {
                                if (treeArg.getKind() == Tree.Kind.IDENTIFIER){
                                    IdentifierTree identifierTree = (IdentifierTree)treeArg;
                                    field.getTypeParam().add(identifierTree.getName().toString());
                                }
                            });
                        }

                        if (field.getType() != null){
                            result.add(field);
                        }
                    }
                }
            }
        }

        return result;
    }
}
