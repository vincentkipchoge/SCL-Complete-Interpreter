package pkg;

public class Constants {
    private Constants(){
    }
    // constructor made private to prevent instantiation
    final public static int EOF = 0;

    final public static int IDENT = 2;
    final public static int NUMBER = 3;
    final public static int STRING = 5;

    final public static int LF = 10;
    final public static int ADDOP = 17;
    final public static int SUBOP = 18;
    final public static int STAROP = 19;
    final public static int DIVOP = 20;
    final public static int EQUAOP = 21;
    final public static int LTHAN = 22;
    final public static int GTHAN = 23;
    final public static int EQUATOO = 24;
    final public static int COMMA = 25;

    final public static int INTEGER = 30;
    final public static int TUNSIGNED = 31;
    final public static int TSTRING = 32;

    final public static int BEGIN = 40;
    final public static int DECREMENT = 41;
    final public static int INCREMENT = 42;
    final public static int DEFINE = 43;
    final public static int DISPLAY = 44;
    final public static int DO = 45;
    final public static int ELSE = 46;
    final public static int IF = 47;
    final public static int THEN = 51;
    final public static int ENDIF = 48;
    final public static int WHILE = 49;
    final public static int ENDWHILE = 50;
    final public static int FUNCTION = 52;
    final public static int ENDFUNCTION = 53;
    final public static int IS = 54;
    final public static int INPUT = 55;
    final public static int SET = 56;
    final public static int VARIABLES = 57;
    final public static int OF = 58;
    final public static int TYPE = 59;
    final public static int MAIN = 62;
}
