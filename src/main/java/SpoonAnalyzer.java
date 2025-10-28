// src/main/java/SpoonAnalyzer.java
import metrics.MetricsCalculator;
import model.ClassInfo;
import model.FieldInfo;
import model.MethodCallInfo;
import model.MethodInfo;
import processors.*;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import ui.MetricsUI;

import java.io.File;
import java.util.*;

public class SpoonAnalyzer {

    public static List<ClassInfo> analyze(String projectSourcePath, String[] classpath) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(projectSourcePath);

        launcher.getEnvironment().setComplianceLevel(17);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setAutoImports(false);
        launcher.getEnvironment().setCommentEnabled(false);

        CtModel model = launcher.buildModel();

        ModelRepo repo = new ModelRepo();
        launcher.addProcessor(new TypeProcessor(repo));
        launcher.addProcessor(new FieldProcessor(repo));
        launcher.addProcessor(new MethodProcessor(repo));
        launcher.addProcessor(new CtorProcessor(repo));
        launcher.addProcessor(new CallProcessor(repo));
        launcher.addProcessor(new CallProcessor.CtorCallProcessor(repo));

        launcher.process();

        return repo.asList();
    }

    // Démo rapide
    public static void main(String[] args) {
        String projectSrc = "/home/loris/Documents/S9/Evolution et Restructuration des logiciels/TP2-ComprehensionLogiciel/src"; // adapte
        String[] cp = getClassPath();

        List<ClassInfo> out = analyze(projectSrc, cp);
        MetricsCalculator.Metrics result = MetricsCalculator.compute(out, null, null);
        MetricsUI.show(result, out, 5);
        for (ClassInfo ci : out) {
            System.out.println("=== " + (ci.qualifiedName != null ? ci.qualifiedName : ci.className) + " ===");
            for (MethodInfo m : ci.methods) {
                System.out.println("  " + m.visibility + " " + (m.returnType != null ? m.returnType : "void")
                        + " " + m.name + "(" + String.join(", ", m.parameterTypes) + ")  loc=" + m.loc);
                if (m.calls != null) {
                    for (MethodCallInfo c : m.calls) {
                        System.out.println("    -> call " + c.name
                                + "  recv=" + c.receiverStaticType
                                + "  decl=" + c.declaringType
                                + "  sig=" + c.qualifiedSignature);
                    }
                }
            }
        }
    }

    private static String[] getClassPath() {
        String JAVA_HOME = "/home/loris/.jdks/corretto-17.0.16";
        String[] cp = new String[]{
                JAVA_HOME + "/jmods/java.base.jmod",
                JAVA_HOME + "/jmods/java.desktop.jmod",
                JAVA_HOME + "/jmods/java.logging.jmod",
                JAVA_HOME + "/jmods/java.xml.jmod",
                JAVA_HOME + "/jmods/java.sql.jmod",
                JAVA_HOME + "/jmods/java.management.jmod",
                JAVA_HOME + "/jmods/java.naming.jmod",
                JAVA_HOME + "/jmods/java.net.http.jmod"
        }; // ajoute tes jars/dossiers de classes si besoin
        return cp;
    }
}
