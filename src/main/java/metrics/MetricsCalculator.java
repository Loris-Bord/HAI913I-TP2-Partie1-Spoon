package metrics;

import model.ClassInfo;
import model.MethodCallInfo;
import model.MethodInfo;

import java.util.*;
import java.util.stream.Collectors;

public class MetricsCalculator {
    public static class Metrics {
       public float couplage;
    }

    public static Metrics compute(List<ClassInfo> classes, Map<String, Integer> filePathToLOC, Set<String> packages) {
        Metrics m = new Metrics();



        return m;
    }

    private static String qnOf(ClassInfo ci) {
        if (ci.qualifiedName != null && !ci.qualifiedName.isEmpty()) return ci.qualifiedName;
        return (ci.packageName != null && !ci.packageName.isEmpty())
                ? ci.packageName + "." + ci.className
                : ci.className;
    }

    private static String simpleSig(MethodInfo m) {
        return m.name + "(" + (m.parameterTypes == null ? "" : String.join(",", m.parameterTypes)) + ")";
    }

    /**
     * Calcule une métrique de couplage
     * @param methodGraph
     * @param classes
     * @param A
     * @param B
     * @return
     */
    public static float calculateCoupling(CallGraphBuilder.DiGraph<String> methodGraph,
                                          List<ClassInfo> classes,
                                          ClassInfo A, ClassInfo B) {
        if (methodGraph == null || classes == null || A == null || B == null || A == B) return 0f;

        Map<String,String> simple2fqn = new HashMap<>();
        Set<String> fqns = new HashSet<>();
        for (ClassInfo ci : classes) {
            String fqn = qnOf(ci);
            fqns.add(fqn);
            simple2fqn.put(ci.className, fqn);
        }

        String qnA = qnOf(A);
        String qnB = qnOf(B);

        long numerator = 0, denominator = 0;

        for (Map.Entry<String, Set<String>> e : methodGraph.edges().entrySet()) {
            String fromOwner = canonicalOwner(ownerOfMethodNode(e.getKey()), fqns, simple2fqn);
            if (fromOwner.isEmpty()) continue;
            for (String toNode : e.getValue()) {
                String toOwner = canonicalOwner(ownerOfMethodNode(toNode), fqns, simple2fqn);
                if (toOwner.isEmpty()) continue;

                denominator++;

                if ((fromOwner.equals(qnA) && toOwner.equals(qnB)) ||
                        (fromOwner.equals(qnB) && toOwner.equals(qnA))) {
                    numerator++;
                }
            }
        }
        System.out.println(numerator + "/" + denominator);
        return denominator == 0 ? 0f : (float) numerator / (float) denominator;
    }

// --- helpers ---

    private static String canonicalOwner(String owner,
                                         Set<String> projectFqns,
                                         Map<String, String> simple2fqn) {
        if (owner == null) return "";
        owner = owner.trim().replace('$','.');
        if (owner.isEmpty()) return "";

        if (projectFqns.contains(owner)) return owner;

        if (owner.indexOf('.') < 0) {
            String fqn = simple2fqn.get(owner);
            return (fqn != null) ? fqn : "";
        }

        return projectFqns.contains(owner) ? owner : "";
    }

    private static String ownerOfMethodNode(String nodeLabel) {
        if (nodeLabel == null) return "";
        String s = nodeLabel.trim();
        int paren = s.indexOf('(');
        if (paren <= 0) return "";
        int lastDot = s.lastIndexOf('.', paren);
        if (lastDot <= 0) return "";
        return s.substring(0, lastDot).replace('$','.');
    }


    /** Privilégie la classe déclarant la méthode; fallback sur le type statique du receveur. */
    private static String targetType(MethodCallInfo c) {
        if (c.declaringType != null && !c.declaringType.isEmpty()) return c.declaringType;
        return (c.receiverStaticType != null && !c.receiverStaticType.isEmpty()) ? c.receiverStaticType : null;
    }

    private static long getNbReferenceBetweenClasses(ClassInfo A, ClassInfo B, String qnA, String qnB) {
        long ab = A.methods.stream()
                .flatMap(m -> m.calls.stream())
                .filter(call -> isCallTo(call, qnB)) // A -> B
                .count();

        long ba = B.methods.stream()
                .flatMap(m -> m.calls.stream())
                .filter(call -> isCallTo(call, qnA)) // B -> A
                .count();

        return ab + ba;
    }

    private static boolean isInterClass(MethodCallInfo call, String ownerQN) {
        String decl = call.declaringType;
        String recv = call.receiverStaticType;
        // cible résolue ?
        String target = (decl != null && !decl.isEmpty()) ? decl : recv;
        return target != null && !target.equals(ownerQN);
    }

    private static boolean isCallTo(MethodCallInfo call, String targetQN) {
        if (targetQN == null) return false;
        if (targetQN.equals(call.declaringType)) return true;
        return targetQN.equals(call.receiverStaticType);
    }
}
