package util.fields;


public class IntFieldName extends FieldName<Integer> {

    private static final long serialVersionUID = 1L;

    public IntFieldName(String name) {
        super(name);
    }

    public IntFieldName(String name, int defalt) {
        super(name, defalt);
    }

    @Override public Integer fromJmsString(String jms) {
        return Integer.valueOf(jms);
    }

    @Override public String toJmsString(Integer value) {
        return value == null ? "" : "" + value;
    }

}
