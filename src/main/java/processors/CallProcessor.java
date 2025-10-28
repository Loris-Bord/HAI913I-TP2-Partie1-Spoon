package processors;

import model.MethodCallInfo;
import model.MethodInfo;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

public class CallProcessor extends AbstractProcessor<CtInvocation<?>> {
    private final ModelRepo repo;
    public CallProcessor(ModelRepo repo) { this.repo = repo; }

    @Override public void process(CtInvocation<?> inv) {
        CtExecutableReference<?> ref = inv.getExecutable();

        MethodCallInfo call = new MethodCallInfo();
        call.name = (ref != null) ? ref.getSimpleName() : null;

        CtTypeReference<?> decl = (ref != null) ? ref.getDeclaringType() : null;
        call.declaringType = decl != null ? decl.getQualifiedName() : null;

        call.receiverStaticType =
                (inv.getTarget() != null && inv.getTarget().getType() != null)
                        ? inv.getTarget().getType().getQualifiedName()
                        : call.declaringType;

        call.methodKey = (ref != null) ? ref.toString() : null;
        call.qualifiedSignature = call.methodKey;

        CtExecutable<?> parent = inv.getParent(CtExecutable.class);
        if (parent != null && parent.getReference() != null) {
            MethodInfo where = repo.methodsByKey.get(parent.getReference().toString());
            if (where != null) where.calls.add(call);
        }
    }

    @Override public void processingDone() {
    }

    public static class CtorCallProcessor extends AbstractProcessor<CtConstructorCall<?>> {
        private final ModelRepo repo;
        public CtorCallProcessor(ModelRepo repo) { this.repo = repo; }

        @Override public void process(CtConstructorCall<?> cc) {
            MethodCallInfo call = new MethodCallInfo();
            call.name = "<init>";

            CtExecutableReference<?> ref = cc.getExecutable();
            CtTypeReference<?> decl = (ref != null ? ref.getDeclaringType() : cc.getType());

            call.declaringType = decl != null ? decl.getQualifiedName() : null;
            call.receiverStaticType = call.declaringType;
            call.methodKey = (ref != null) ? ref.toString() : null;
            call.qualifiedSignature = (ref != null) ? ref.toString()
                    : (decl != null ? decl.getQualifiedName() + ".<init>(?)" : "<init>(?)");

            CtExecutable<?> parent = cc.getParent(CtExecutable.class);
            if (parent != null && parent.getReference() != null) {
                MethodInfo where = repo.methodsByKey.get(parent.getReference().toString());
                if (where != null) where.calls.add(call);
            }
        }
    }
}

