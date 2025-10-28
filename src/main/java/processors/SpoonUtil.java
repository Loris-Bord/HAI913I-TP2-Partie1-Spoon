package processors;


import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.ModifierKind;

public class SpoonUtil {
    public static String visibilityOf(ModifierKind v) {
        if (v == null) return "package";
        switch (v) {
            case PUBLIC: return "public";
            case PROTECTED: return "protected";
            case PRIVATE: return "private";
            default: return "package";
        }
    }

    public static int safeLOC(CtElement e) {
        if (e == null || e.getPosition() == null || !e.getPosition().isValidPosition()) return 0;
        return Math.max(0, e.getPosition().getEndLine() - e.getPosition().getLine() + 1);
    }

    public static boolean isTopLevel(CtTypeInformation ti) {
        if (ti == null) return false;
        if (ti.isAnonymous() || ti.isLocalType()) return false;
        if (ti instanceof CtType) return ((CtType<?>) ti).getDeclaringType() == null;
        return false;
    }
}
