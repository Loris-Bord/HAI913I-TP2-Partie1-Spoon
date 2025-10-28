package processors;

import model.MethodInfo;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;

public class CtorProcessor extends AbstractProcessor<CtConstructor<?>> {
    private final ModelRepo repo;
    public CtorProcessor(ModelRepo repo) { this.repo = repo; }

    @Override public void process(CtConstructor<?> k) {
        var type = k.getDeclaringType();
        if (type == null || !SpoonUtil.isTopLevel(type)) return;
        var ci = repo.ensureClass(type.getQualifiedName(),
                type.getPackage() != null ? type.getPackage().getQualifiedName() : "",
                type.getSimpleName());

        MethodInfo mi = new MethodInfo();
        mi.name = "<init>";
        mi.declaringType = ci.qualifiedName;
        mi.returnType = "void";
        mi.visibility = SpoonUtil.visibilityOf(k.getVisibility());

        mi.parameterTypes = new ArrayList<>();
        for (CtParameter<?> p : k.getParameters()) {
            CtTypeReference<?> tr = p.getType();
            mi.parameterTypes.add(tr != null ? tr.getQualifiedName() : "?");
        }
        mi.parametersCount = mi.parameterTypes.size();
        mi.loc = SpoonUtil.safeLOC(k);
        mi.calls = new ArrayList<>();

        CtExecutableReference<?> ref = k.getReference();
        mi.methodKey = (ref != null) ? ref.toString()
                : ci.qualifiedName + "#<init>(" + String.join(",", mi.parameterTypes) + ")";
        mi.qualifiedSignature = mi.methodKey;

        ci.methods.add(mi);
        repo.methodsByKey.put(mi.methodKey, mi);
    }
}
