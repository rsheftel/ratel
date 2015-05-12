package util.fields;


public class StringFieldName extends FieldName<String> {

    private static final long serialVersionUID = 1L;

    public StringFieldName(String name) {
        super(name);
    }

    public StringFieldName(String name, String defalt) {
        super(name, defalt);
    }

    @Override public String fromJmsString(String jms) {
        return jms;
    }

    @Override public String toJmsString(String value) {
        return value;
    }

}
