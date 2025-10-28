package processors;

import model.FieldInfo;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;

public class FieldProcessor extends AbstractProcessor<CtField<?>> {
    private final ModelRepo repo;
    public FieldProcessor(ModelRepo repo) { this.repo = repo; }

    @Override public void process(CtField<?> f) {
        var type = f.getDeclaringType();
        if (type == null || !SpoonUtil.isTopLevel(type)) return;
        var ci = repo.ensureClass(type.getQualifiedName(),
                type.getPackage() != null ? type.getPackage().getQualifiedName() : "",
                type.getSimpleName());

        FieldInfo fi = new FieldInfo();
        fi.name = f.getSimpleName();
        fi.visibility = SpoonUtil.visibilityOf(f.getVisibility());

        ci.fields.add(fi);
    }
}
