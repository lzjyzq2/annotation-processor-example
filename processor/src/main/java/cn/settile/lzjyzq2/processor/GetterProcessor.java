package cn.settile.lzjyzq2.processor;

import cn.settile.lzjyzq2.processor.annotation.Getter;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
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

@SupportedAnnotationTypes("cn.settile.lzjyzq2.processor.annotation.Getter")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class GetterProcessor extends BaseProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager.printMessage(Diagnostic.Kind.NOTE, "========= GetterProcessor init =========");
    }

    /**
     * 对 AST 进行处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取被自定义 Getter 注解修饰的元素
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Getter.class);
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
                    // 为成员变量构造 getter 方法，并添加到 JCClassDecl 之中
                    jcVariableDeclList.forEach(jcVariableDecl -> {
                        messager.printMessage(Diagnostic.Kind.NOTE, jcVariableDecl.getName() + " has been processed");
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetterMethodDecl(jcVariableDecl));
                    });
                    super.visitClassDef(jcClassDecl);
                }

            });
        });
        return true;
    }

    /**
     * 为成员遍历构造 getter 方法
     */
    private JCTree.JCMethodDecl makeGetterMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        // 生成表达式 return this.value;
        statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())));
        // 加上大括号 { return this.value; }
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        // 组装方法
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getNewMethodName(jcVariableDecl.getName()), jcVariableDecl.vartype, List.nil(), List.nil(), List.nil(), body, null);
    }

    /**
     * 驼峰命名法
     */
    private Name getNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("get" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }
}
