package processors;

import model.ClassInfo;
import model.MethodInfo;

import java.util.*;

public class ModelRepo {
    public final Map<String, ClassInfo> classesByFqn = new LinkedHashMap<>();
    public final Map<String, MethodInfo> methodsByKey = new HashMap<>();

    public List<ClassInfo> asList() {
        return new ArrayList<>(classesByFqn.values());
    }

    public ClassInfo ensureClass(String fqn, String pkg, String simpleName) {
        return classesByFqn.computeIfAbsent(fqn, q -> {
            ClassInfo ci = new ClassInfo();
            ci.qualifiedName = q;
            ci.packageName   = pkg != null ? pkg : "";
            ci.className     = simpleName != null ? simpleName : q;
            ci.superClassesChain = new ArrayList<>();
            ci.fields  = new ArrayList<>();
            ci.methods = new ArrayList<>();
            return ci;
        });
    }
}
