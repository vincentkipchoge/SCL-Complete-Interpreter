package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import static pkg.Constants.*;

public class Scanner {
    private boolean eofReached;
    private boolean EOL;
    public int count = 0;

    private int tokenType = 1;
    private FileReader fr;
    private int row, col;
    private int errorCount;
    //accessed to retrieve the current symbol
    public String currentLexeme;

    public Scanner(File in){
        eofReached = false;
        EOL = false;
        errorCount = 0;
        row = 1;
        col = 1;
        try {
            fr = new FileReader(in);
        } catch (FileNotFoundException e) {
            System.out.println("File '"+in.getPath()+"' was not able to be found.");
        }
    }
    //numeric value produced is the token code for the next symbol
    public int nextToken() {

        count++;
        currentLexeme = getSymbol();
        if (eofReached)
            return 0;
        switch (tokenType){
            case 1:
                int tokenCode = lookup(currentLexeme);
                if (tokenCode == -1)
                    return 2;
                return tokenCode;
            case 2:
                return IDENT;
            case 3:
                return NUMBER;
            case 4:
                return lookup(currentLexeme);
            case 5:
                return STRING;
            default:
                return lookup(currentLexeme);
            /*
                Default statement should never be reached
                getSymbol() function will handle errors so the tokenType variable
                should always be 1 through 4.
            */

        }

    }

    public int getCol(){
        return col;
    }
    public int getRow(){
        return row;
    }

    private String getSymbol() {
        tokenType = 1;          // type is assumed to be keyword initially
        String symbol = "";
        if (EOL){
            // Statement needed to account for new line immediately following a symbol
            EOL = false;
            row++;
            col = 0;

        }

        char c = getChar();

        int state = 30;     //initial state arbitrarily 30

        while(true){
            OUTER:
            switch (state) {
                case 30:
                    //skips over space characters
                    if ((int)c == 32){
                        c = getChar();
                        break;
                    // 6000 is the arbitrary end of file character.
                    } else if ((int) c == 6000){
                        eofReached = true;
                        return "\\EOF";
                    //when special characters are found, refer to state 12
                    } else if (c=='+'||c=='-'||c=='*'||c=='/'||c=='<'||c=='>'||c==','||c=='='||c=='"'){
                        tokenType = 4;
                        state = 12;
                        break;
                        //could be keyword or identifier
                    } else if (Character.isLetter(c)){
                        state = 15;
                        break;
                    //number (integer) literal
                    } else if (Character.isDigit(c)){
                        state = 16;
                    //upon newline, keep track of position of reader
                    } else if ((int) c == 10 || (int) c == 13){
                        row++;
                        col = 0;

                        c = getChar();
                        break;
                    //if eof reached without reading escape character 6000
                    } else if (eofReached)
                        return "\\EOF";
                    //in the case that no other state is matched, error state reached
                    else state = -5;
                    break;
                case 12:
                    // interpret special characters
                    switch (c) {
                        case '"':
                            //case handles string literals
                            tokenType = 5;
                            symbol = symbol.concat(Character.toString(c));
                            c = getChar();
                            while(c != '"'&&(int)c != 10&&(int) c != 13){
                                symbol = symbol.concat(Character.toString(c));
                                c = getChar();
                            }   if ((int)c == 10 || (int) c == 13) {
                            //if newline found before closing ", error encountered
                            state = -5;
                            break OUTER;
                        }
                            symbol = symbol.concat(Character.toString(c));
                            return symbol;
                        //These cases used for comparison operators
                        case '>':
                        case '<':
                        case '=':
                            symbol = symbol.concat(Character.toString(c));
                            c = getChar();
                            //look for a second '=' after a comparison symbol
                            if (c == '='){
                                symbol = symbol.concat(Character.toString(c));
                                return symbol;
                            }
                            return symbol;
                        default:
                            symbol = symbol.concat(Character.toString(c));
                            return symbol;
                    }
                case 15:
                    //Case for building keywords/identifiers
                    symbol = symbol.concat(Character.toString(c));
                    c = getChar();
                    while (Character.isLetterOrDigit(c)){
                        // if a digit is encountered, type is moved from keyword to identifier
                        if (Character.isDigit(c))
                            tokenType = 2;
                        symbol = symbol.concat(Character.toString(c));
                        c = getChar();
                    }
                    //space, end of line, or end of file cause symbol to return
                    if (c == ' '){
                        return symbol;
                    } else if ((int) c == 10 || (int) c == 13) {
                        EOL = true;
                        return symbol;
                    } else if ((int) c == 6000){
                        return symbol;
                    } else {
                        //error if character other than alphanumeric or whitespace encountered
                        state = -5;
                        break;
                    }
                case 16:
                    //case for building integer literals
                    tokenType = 3;
                    symbol = symbol.concat(Character.toString(c));
                    c = getChar();
                    while (Character.isDigit(c)){
                        symbol = symbol.concat(Character.toString(c));
                        c = getChar();
                    }
                    //space, end of line, or end of file cause symbol to return
                    if (c == ' '){
                        return symbol;
                    } else if ((int) c == 10 || (int) c == 13) {
                        EOL = true;
                        return symbol;
                    } else if ((int) c == 6000){
                        return symbol;
                    } else {
                        //error state envoked if character other than digit or whitespace encountered
                        state = -5;
                        break;
                    }
                case -5:
                    //error handling case.
                    errorCount++;
                    //If an unidentified symbol is found, skips to next whitespace
                    while (!(c == ' ')&&!eofReached){
                        if ((int)c == 10 || (int) c == 13){
                            row++;
                            break;
                        }
                        c = getChar();
                    }
                    return getSymbol();
                default:
                    //Shouldn't be invoked, but needed for mandatory method return and testing
                    return "\\DEF";
            }
        }       // End of While
    }

    private char getChar(){
        try  {
            int x = fr.read();  //reads the next character from the input file
            if (x == -1){
                eofReached = true;
                return 6000;

            }
            col++;
            return (char) x;
        } catch (IOException e) {}
        return '~';
    }
    //search of keywords
    private int lookup(String s){

        //returns the corresponding constant from the kwConst array
        //or -1 if the keyword is not found.
        for (int i = 0; i < kwords.length; i++){
            if(s.compareTo(kwords[i])==0)
                return kwConst[i];
        }
        return -1;
    }
    //keywords
    private String[] kwords = {
            "begin",
            "decrement",
            "define",
            "display",
            "do",
            "else",
            "endfun",
            "endif",
            "endwhile",
            "function",
            "if",
            "increment",
            "input",
            "integer",
            "is",
            "of",
            "set",
            "then",
            "type",
            "variables",
            "while",
            "+",
            "-",
            "*",
            "/",
            "=",
            "<",
            ">",
            "\\LF",
            "main",
            "==",
            ",",
            "string"
    };
    //keywords constants
    private int[] kwConst = {
            BEGIN,
            DECREMENT,
            DEFINE,
            DISPLAY,
            DO,
            ELSE,
            ENDFUNCTION,
            ENDIF,
            ENDWHILE,
            FUNCTION,
            IF,
            INCREMENT,
            INPUT,
            INTEGER,
            IS,
            OF,
            SET,
            THEN,
            TYPE,
            VARIABLES,
            WHILE,
            ADDOP,
            SUBOP,
            STAROP,
            DIVOP,
            EQUAOP,
            LTHAN,
            GTHAN,
            LF,
            MAIN,
            EQUATOO,
            COMMA,
            TSTRING
    };
    public int getErrors(){
        return errorCount;
    }
    public boolean endOfFileReached(){
        return eofReached;
    }
    //returns a string representation that corresponds to the token type
    public String convertType(int i){

        switch (i){
            case -1:
                return "unidentified symbol";
            case 0:
                return "End of file";
            case 2:
                return "identifier";
            case 3:
                return "number";
            case 5:
                return "String literal";
            case 17:
                return "add operator";
            case 18:
                return "subtract operator";
            case 19:
                return "multiply operator";
            case 20:
                return "divide operator";
            case 21:
                return "assignment operator";
            case 22:
                return "less than operator";
            case 23:
                return "greater than operator";
            case 24:
                return "equal to operator";
            case 25:
                return "comma ";
            default:
                return "keyword";
        }
    }
}
