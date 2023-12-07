package pkg;

import pkg.Identifier;

public class IntegerIdentifier extends Identifier {
    private int value;

    public IntegerIdentifier(String name, int type, int value) {
        super(name, type);
        this.value = value;
    }
    @Override
    public String getValue(){
        return Integer.toString(value);
    }
    @Override
    public void setValue(String newValue){
        value = Integer.parseInt(newValue);
    }
    @Override
    public String toString(){
        return "["+name+","+type+","+value+"]";
    }
    // second set of accessor/mutator for retrieving/setting value as integer not string
    public int getIntValue(){
        return value;
    }
    public void setIntValue(int newValue){
        value = newValue;
    }
}
