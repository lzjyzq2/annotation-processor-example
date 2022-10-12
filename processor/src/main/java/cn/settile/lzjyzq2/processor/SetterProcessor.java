package cn.settile.lzjyzq2.processor;

import cn.settile.lzjyzq2.processor.annotation.Setter;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * @author lzjyz
 */
@SupportedAnnotationTypes("cn.settile.lzjyzq2.processor.annotation.Setter")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class SetterProcessor extends BaseProcessor{

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager.printMessage(Diagnostic.Kind.NOTE, "========= SetterProcessor init =========");
    }

    /**
     * 对 AST 进行处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取被自定义 Setter 注解修饰的元素
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Setter.class);
        set.forEach(element -> {
            // 根据元素获取对应的语法树 JCTree
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                // 处理语法树的类定义部分 JCClassDecl
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();
                    for (JCTree tree : jcClassDecl.defs) {
                        // 找到语法树上的成员变量节点，存储到 jcVariableDeclList 集合
                        if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
                            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) tree;
                            jcVariableDeclList = jcVariableDeclList.append(jcVariableDecl);
                        }
                    }
                    // 为成员变量构造 setter 方法，并添加到 JCClassDecl 之中
                    jcVariableDeclList.forEach(jcVariableDecl -> {
                        messager.printMessage(Diagnostic.Kind.NOTE, jcVariableDecl.getName() + " has been processed");
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeSetterMethodDecl(jcVariableDecl));
                    });
                    super.visitClassDef(jcClassDecl);
                }
            });
        });
        return true;
    }

    /**
     * 为成员构造 setter 方法
     */
    private JCTree.JCMethodDecl makeSetterMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        // 生成表达式 this.value = value;
        JCTree.JCExpressionStatement aThis = makeAssignment(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName()), treeMaker.Ident(jcVariableDecl.getName()));
        statements.append(aThis);
        // 加上大括号 { this.value = value; }
        JCTree.JCBlock block = treeMaker.Block(0, statements.toList());

        // 生成方法参数之前，指明当前语法节点在语法树中的位置，避免出现异常 java.lang.AssertionError: Value of x -1
        treeMaker.pos = jcVariableDecl.pos;

        // 生成方法参数 String value
        JCTree.JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), jcVariableDecl.getName(), jcVariableDecl.vartype, null);
        List<JCTree.JCVariableDecl> parameters = List.of(param);
        // 生成返回对象 void
        JCTree.JCExpression methodType = treeMaker.Type(new Type.JCVoidType());

        // 组装方法
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getNewMethodName(jcVariableDecl.getName()), methodType, List.nil(), parameters, List.nil(), block, null);
    }

    /**
     * 赋值操作 lhs = rhs
     */
    private JCTree.JCExpressionStatement makeAssignment(JCTree.JCExpression lhs, JCTree.JCExpression rhs) {
        return treeMaker.Exec(
                treeMaker.Assign(lhs, rhs)
        );
    }

    /**
     * 驼峰命名法
     */
    private Name getNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("set" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }
}
