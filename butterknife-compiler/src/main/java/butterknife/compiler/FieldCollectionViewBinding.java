package butterknife.compiler;

import com.squareup.javapoet.TypeName;

/**
 * 视图列表绑定信息
 */
final class FieldCollectionViewBinding implements ViewBinding {
    enum Kind {
        ARRAY,
        LIST
    }

    private final String name;
    private final TypeName type;
    private final Kind kind;

    FieldCollectionViewBinding(String name, TypeName type, Kind kind) {
        this.name = name;
        this.type = type;
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public String getDescription() {
        return "field '" + name + "'";
    }
}
