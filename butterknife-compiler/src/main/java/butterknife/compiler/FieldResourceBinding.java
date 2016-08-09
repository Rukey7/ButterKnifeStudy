package butterknife.compiler;

/**
 * 资源信息
 */
public final class FieldResourceBinding {

    private final int id;
    private final String name;
    private final String method;
    private final boolean themeable;

    public FieldResourceBinding(int id, String name, String method, boolean themeable) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.themeable = themeable;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    public boolean isThemeable() {
        return themeable;
    }
}
