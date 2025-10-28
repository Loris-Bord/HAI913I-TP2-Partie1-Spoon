package processors;

import model.MethodInfo;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;

public class MethodProcessor extends AbstractProcessor<CtMethod<?>> {
    private final ModelRepo repo;
    public MethodProcessor(ModelRepo repo) { this.repo = repo; }

    @Override public void process(CtMethod<?> m) {
        var type = m.getDeclaringType();
        if (type == null || !SpoonUtil.isTopLevel(type)) return;
        var ci = repo.ensureClass(type.getQualifiedName(),
                type.getPackage() != null ? type.getPackage().getQualifiedName() : "",
                type.getSimpleName());

        MethodInfo mi = new MethodInfo();
        mi.name = m.getSimpleName();
        mi.declaringType = ci.qualifiedName;
        mi.returnType = m.getType() != null ? m.getType().getQualifiedName() : "void";
        mi.visibility = SpoonUtil.visibilityOf(m.getVisibility());

        mi.parameterTypes = new ArrayList<>();
        for (CtParameter<?> p : m.getParameters()) {
            CtTypeReference<?> tr = p.getType();
            mi.parameterTypes.add(tr != null ? tr.getQualifiedName() : "?");
        }
        mi.parametersCount = mi.parameterTypes.size();
        mi.loc = SpoonUtil.safeLOC(m);
        mi.calls = new ArrayList<>();

        CtExecutableReference<?> ref = m.getReference();
        mi.methodKey = (ref != null) ? ref.toString()
                : ci.qualifiedName + "#" + mi.name + "(" + String.join(",", mi.parameterTypes) + ")";
        mi.qualifiedSignature = mi.methodKey;

        ci.methods.add(mi);
        repo.methodsByKey.put(mi.methodKey, mi);
    }
}
