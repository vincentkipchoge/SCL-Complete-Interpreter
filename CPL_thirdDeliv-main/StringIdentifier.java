package pkg;

public class StringIdentifier extends Identifier{
    private String value;

    public StringIdentifier(String name, int type, String value) {
        super(name, type);
        this.value = value;
    }
    @Override
    public String getValue(){
        return value;
    }
    @Override
    public void setValue(String newValue){
        value = newValue;
    }
    @Override
    public String toString(){
        return "["+name+","+type+","+value+"]";
    }
}
