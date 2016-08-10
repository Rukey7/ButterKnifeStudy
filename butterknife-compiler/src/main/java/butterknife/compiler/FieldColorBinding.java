package butterknife.compiler;

/**
 * Created by long on 2016/8/10.
 */
public class FieldColorBinding {

    private final int id;
    private final String name;
    private final String method;

    public FieldColorBinding(int id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
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
}
