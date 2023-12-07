package pkg;

public abstract class Identifier {
    final protected String name;
    final protected int type;

    public Identifier(String name, int type){
        this.name = name;
        this.type = type;
    }
    public String getName(){
        return name;
    }
    public int getType(){
        return type;
    }

    public abstract String getValue();
    public abstract void setValue(String value);
}
