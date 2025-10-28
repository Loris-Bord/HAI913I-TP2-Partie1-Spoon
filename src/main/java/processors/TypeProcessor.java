package processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

public class TypeProcessor extends AbstractProcessor<CtType<?>> {
    private final ModelRepo repo;
    public TypeProcessor(ModelRepo repo) { this.repo = repo; }

    @Override public boolean isToBeProcessed(CtType<?> t) {
        if (!(t instanceof CtClass || t instanceof CtInterface || t instanceof CtEnum)) return false;
        return SpoonUtil.isTopLevel(t);
    }


    @Override public void process(CtType<?> t) {
        String fqn = t.getQualifiedName();
        String pkg = t.getPackage() != null ? t.getPackage().getQualifiedName() : "";
        String sn  = t.getSimpleName();

        var ci = repo.ensureClass(fqn, pkg, sn);

        // super classes / interfaces (chaîne simplifiée)
        if (t instanceof CtClass) {
            CtClass<?> c = (CtClass<?>) t;
            if (c.getSuperclass() != null) ci.superClassesChain.add(c.getSuperclass().getQualifiedName());
            for (CtTypeReference<?> s : c.getSuperInterfaces()) ci.superClassesChain.add(s.getQualifiedName());
        } else if (t instanceof CtInterface) {
            for (CtTypeReference<?> s : ((CtInterface<?>) t).getSuperInterfaces())
                ci.superClassesChain.add(s.getQualifiedName());
        }
    }
}
