package pkg;
import java.io.File;
import java.util.ArrayList;
import static pkg.Constants.*;

public class Parser {
    //scanner
    Scanner scanner;
    // boolean value to indicate when end of file has been reached
    boolean EOF;
    //stores the most recent token
    private int nextTok;
    //stores the most recent lexeme
    private String nextLex;
    //table to store identifiers as they're created.
    protected ArrayList<Identifier> idTable;
    //ArrayList for current statement
    private final ArrayList<String> currStatement;
    //value of most recent identifier
    private int activeIdentifier;
    //for use in declaration of identifiers at execution time
    private String activeIdentifierName;
    //object used as arbitrary reference for new assignment statements
    private AssignmentStatement assignmentStatement;
    private final File f;

    java.util.Scanner inputHandler;

    boolean running = false;
    boolean displayParsedLines = true;
    boolean whileSkip = false;

    //constructor
    public Parser(String in){

        f = new File(in);
        inputHandler = new java.util.Scanner(System.in);
        this.nextLex = "";
        this.idTable = new ArrayList();
        this.EOF = false;
        this.scanner = new Scanner(f);
        this.currStatement = new ArrayList();

    }

    //calls the scanner
    public void scan(){
        nextTok = scanner.nextToken();
        nextLex = scanner.currentLexeme;
    }
    public boolean endOfFileReached(){
        return EOF;
    }
    // Method prints out error messages for the parse.
    private void error(String str, int loc, boolean skip){


        System.err.println(str+"; line "+loc);
        if (skip){
            int x = scanner.getRow();
            while (x == scanner.getRow()){
                scan();
            }
        }
    }
    // return complete ID table
    public void printIdTable(){
        idTable.forEach((i) -> {
            System.out.println(i.toString());
        });
    }
    private boolean lookup(String str){
        //see if string is already in identifier table

        activeIdentifierName = nextLex;
        for(Identifier id : idTable){
            if (str.compareTo(id.getName()) == 0){

                activeIdentifier = idTable.indexOf(id);
                return true;
            }
        }
        return false;
    }
    //prints the statement that has been built
    public void flush(){
        if (running)
            displayParsedLines = false;
        if (!displayParsedLines){
            currStatement.clear();
            return;
        }
        String out = "";
        for (String s : currStatement){
            out = out.concat(s+" ");
        }
        System.out.println(out);
        currStatement.clear();
    }
    public void output(String out){
        String s = out.replace("\"", "");
        System.out.print(s);
    }

    //Parsing Functions:
    public void parseProgram(){
        //function parses for an entire function
        /* Derived BNF for this method:
           FUNCTION IDENTIFIER IS
            ( | CONSTANTS data_declarations) VARIABLES data_declarations (  | STRUCT data_declarations)
            BEGIN pactions ENDFUN IDENTIFIER
        */
        scan();
        if (nextTok == FUNCTION){
            currStatement.add(nextLex);
            scan();
            if (nextTok == MAIN){       //BNF subset supports 'main' keyword as function name, not identifiers
                currStatement.add(nextLex);
                scan();
                if (nextTok == IS){
                    currStatement.add(nextLex);
                    flush();
                    scan();
                    if (nextTok == VARIABLES){      //nested ifs were used because syntax is rigid
                        currStatement.add(nextLex);
                        flush();
                        scan();
                        variables();                  //all variable declaration statements here
                    }
                    if (nextTok == BEGIN){
                        currStatement.add(nextLex);
                        flush();
                        scan();
                        begin();           //all function body statements here
                        endfun();
                    } else {
                        error("Must begin statement declaration list with 'begin' keyword", scanner.getRow(), false);
                    }
                }
                else error("Invalid Function Declaration", scanner.getRow(), false);
            }
            else error("Function name must be 'main' keyword", scanner.getRow(), false);
        }
        else {
            error("Must contain main function", scanner.getRow(), false);
        }
        if (nextTok == Constants.EOF){          //After parsing for a function, method checks for end of file
            EOF = true;
        }
    }

    //parses for variable declaration statements
    private void variables() {
        /** BNF: comp_declare -> DEFINE data_declaration */
        while(nextTok == DEFINE){
            currStatement.add(nextLex);
            scan();
            dataDeclaration();
        }
    }
    private void dataDeclaration() {
        /** BNF: data_declaration -> IDENTIFIER OF TYPE data_type */
        if (nextTok == IDENT) {
            currStatement.add(nextLex);
            lookup(nextLex);
            scan();
            if (nextTok == OF) {
                currStatement.add(nextLex);
                scan();
                if (nextTok == TYPE){
                    currStatement.add(nextLex);
                    scan();
                    type();
                }
                else {
                    error("Invalid variable declaration", scanner.getRow(), true);
                }
            }
            else {
                error("Invalid variable declaration", scanner.getRow(), true);
            }
        } else {
            error("Invalid Identifier name", scanner.getRow(), true);
        }
    }
    private void type() { // BNF: data_type -> INTEGER | TSTRING
        switch (nextTok){
            case INTEGER:
                // Decision made here to set default value of unassigned integers to 0.
                idTable.add(new IntegerIdentifier(this.activeIdentifierName, Constants.INTEGER, 0));
                currStatement.add(nextLex);
                flush();
                scan();
                return;
            case TSTRING:
                idTable.add(new StringIdentifier(this.activeIdentifierName, Constants.TSTRING, ""));
                currStatement.add(nextLex);
                flush();
                scan();
                return;
            default:
                error("Type not supported.", scanner.getRow(), true);
        }
    }

    //Function Body Parsing:
    private void begin(){
        /* BNF: BEGIN pactions */
        while (nextTok != ENDFUNCTION){
            actions();
        } if (nextTok == ENDFUNCTION) {
            currStatement.add(nextLex);
            scan();
        }
    }
    private void actions() {
        /* BNF: action_def -> SET name_ref EQUOP expr
            | INPUT name_ref
            | DISPLAY pvar_value_list
            | INCREMENT name_ref
            | DECREMENT name_ref
            | IF pcondition THEN pactions opt_else ENDIF
            | WHILE pcondition DO pactions ENDWHIL
        */
        //actions() method is only looking for the first keyword from each RHS to determine proper method to call.
        switch (nextTok){
            case SET:
                currStatement.add(nextLex);
                scan();
                assignment();
                flush();
                break;
            case INPUT:
                currStatement.add(nextLex);
                scan();
                input();
                flush();
                break;
            case DISPLAY:
                currStatement.add(nextLex);
                scan();
                display();
                flush();
                break;
            case INCREMENT:
                currStatement.add(nextLex);
                scan();
                increment();
                break;
            case DECREMENT:
                currStatement.add(nextLex);
                scan();
                decrement();
                break;
            case IF:
                currStatement.add(nextLex);
                scan();
                ifStatement();
                break;
            case WHILE:
                currStatement.add(nextLex);
                scan();
                whileStatement();
                break;
            default:
                //  decision made here to skip to the end of a line if no action method is applicable
                error("Invalid word to begin a statement", scanner.getRow(), true);
                scan();
                break;
        }
    }

    //Actions Parsing:
    private void assignment() {
        /** BNF: SET name_ref EQUOP expr */

        if (nextTok == IDENT){
            // this call is necessary to establish current identifier as active
            lookup(nextLex);

            currStatement.add(nextLex);
            scan();
            if (nextTok == EQUAOP){
                currStatement.add(nextLex);
                scan();
                if(running){
                    Identifier id = idTable.get(activeIdentifier);
                    assignmentStatement = new AssignmentStatement(id, expr());
                } else
                    expr();

            }
            else {
                error("Invalid syntax for assignment operation", scanner.getRow(), true);
            }
        } else {
            error("Invalid syntax for assignment operation", scanner.getRow(), true);
        }
    }
    private void input() {
        /** BNF: INPUT name_ref
         BNF sample contradicts sample file where a string is included.
         Actual implementation here allows for INPUT string_literal COMMA name_ref */
        switch (nextTok) {
            case STRING:
                currStatement.add(nextLex);
                if(running)output(nextLex);
                scan();
                if (nextTok == COMMA){
                    currStatement.add(nextLex);
                    scan();
                    if (nextTok == IDENT){
                        currStatement.add(nextLex);
                        if (running) {
                            lookup(nextLex);
                            String s = inputHandler.nextLine();
                            Identifier id = idTable.get(activeIdentifier);
                            assignmentStatement = new AssignmentStatement(id, s);
                        }
                        scan();
                    } else
                        error("Input statement invalid syntax", scanner.getRow(), true);
                } else
                    error("Input statement invalid syntax", scanner.getRow(), true);
                break;
            case IDENT:
                currStatement.add(nextLex);
                scan();
                break;
            default:
                error("Input statement invalid syntax", scanner.getRow(), true);
                break;
        }
        if(running)output("\n");
    }
    private void display() {
        /** BNF: DISPLAY pvar_value_list */
        /** BNF: pvar_value_list -> expr | pvar_value_list COMMA expr */
        if(running)output(expr());
        else expr();
        while (nextTok == COMMA){
            currStatement.add(nextLex);
            scan();
            if(running)output(" "+expr());
            else expr();
        }
        if (running)output("\n");
    }
    private void increment() {
        /** BNF: INCREMENT name_ref
         BNF: name_ref -> IDENTIFIER */
        if (nextTok == IDENT){
            currStatement.add(nextLex);
            if (running){
                lookup(nextLex);
                IntegerIdentifier id = (IntegerIdentifier) idTable.get(activeIdentifier);
                int val = id.getIntValue();
                val++;
                id.setIntValue(val);
            }
            flush();
            scan();
        }
        else {
            error("Only valid identifiers can be incremented", scanner.getRow(), true);
        }
    }
    private void decrement() {
        /** BNF: DECREMENT name_ref
         BNF: name_ref -> IDENTIFIER */
        if (nextTok == IDENT){
            currStatement.add(nextLex);
            if (running){
                lookup(nextLex);
                IntegerIdentifier id = (IntegerIdentifier) idTable.get(activeIdentifier);
                int val = id.getIntValue();
                val--;
                id.setIntValue(val);
            }
            flush();
            scan();
        }
        else {
            error("Only valid identifiers can be decremented", scanner.getRow(), true);
        }
    }
    private void ifStatement() {
        /** BNF: IF pcondition THEN pactions opt_else ENDIF */
        boolean execute = pcondition();
        if (nextTok == THEN){
            currStatement.add(nextLex);
            flush();
            scan();
            if (!execute)
                running = false;
            while (nextTok != ENDIF && nextTok != ELSE){
                actions();
            }
            if (nextTok == ELSE){
                currStatement.add(nextLex);
                flush();
                scan();

                optionalElse(execute);
            } if (nextTok == ENDIF) {
                currStatement.add(nextLex);
                flush();
                scan();
                if(!whileSkip)
                    running = true;
            }
        }

    }
    private void whileStatement() {
        /** BNF: WHILE pcondition DO pactions ENDWHILE */
        int conditionIterations = scanner.count;
        //boolean set to value of conditional expression
        boolean execute = pcondition();
        if (nextTok == DO){
            currStatement.add(nextLex);
            flush();
            scan();
            // if condition was false, parse through loop
            if (!execute){
                running = false;        // but don't execute any code
                whileSkip = true;
            }
            while (nextTok != ENDWHILE) {
                actions();
            }
            if (execute){

                Parser p = new Parser(f.getAbsolutePath());
                p.displayParsedLines = false;
                p.idTable = this.idTable;
                for (int i = 0; i<conditionIterations; i++)
                    p.scan();

                while(p.pcondition()){
                    p.running = true;
                    p.scan();
                    while(p.nextTok != ENDWHILE){
                        p.actions();
                    }
                    this.idTable = p.idTable;
                    p = new Parser(f.getAbsolutePath());
                    p.displayParsedLines = false;
                    p.idTable = this.idTable;
                    for (int i = 0; i<conditionIterations; i++)
                        p.scan();
                }
            }
            if (nextTok == ENDWHILE){
                currStatement.add(nextLex);
                flush();
                running = true;
                scan();
            }
        }
        else error("Invalid while loop declaration", scanner.getRow(), true);
    }


    private String expr() {
        /** BNF: expr -> term
         | term PLUS term
         | term MINUS term */
        String s = term();
        while (nextTok == ADDOP || nextTok == SUBOP){
            int i = Integer.parseInt(s);
            currStatement.add(nextLex);
            if (nextTok == ADDOP){
                scan();
                String s1 = term();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i + i1);
            }
            else {
                scan();
                String s1 = term();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i - i1);
            }
        }
        return s;
    }
    private String term() {
        /** BNF: term -> punary
         | punary STAR punary
         | punary DIVOP punary */
        String s = punary();
        while (nextTok == STAROP || nextTok == DIVOP){
            int i = Integer.parseInt(s);
            currStatement.add(nextLex);
            if (nextTok == STAROP){
                scan();
                String s1 = punary();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i * i1);
            }
            else {
                scan();
                String s1 = punary();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i / i1);
            }

        }
        return s;
    }
    private String punary() {
        /** BNF: punary -> element | MINUS element */
        if (nextTok == SUBOP){
            currStatement.add(nextLex);
            scan();
            return element();
        } else {
            return element();
        }
    }
    private String element() {
        //This will be where values are given out at execution time
        /** BNF: element -> IDENTIFIER | STRING	| NUMBER */
        switch (nextTok) {
            case STRING:
                currStatement.add(nextLex);
                String s = nextLex;
                scan();
                return s;
            case IDENT:
                currStatement.add(nextLex);
                String s1 = nextLex;
                scan();
                return idTable.get(getIndex(s1)).getValue();
            case NUMBER:
                currStatement.add(nextLex);
                String s2= nextLex;
                scan();
                return s2;
            default:

                return "";
        }

    }
    private int getIndex(String s){
        for(Identifier id : idTable){
            if (s.compareTo(id.getName()) == 0){
                return idTable.indexOf(id);
            }
        }
        return -1;
    }
    private boolean pcondition() {
        /** BNF: pcondition -> expr eq_v expr */
        String exp1 = expr();
        int operation = comparison();
        String exp2 = expr();

        int val1 = Integer.parseInt(exp1);
        int val2 = Integer.parseInt(exp2);
        switch (operation){
            case EQUATOO:
                if (val1 == val2)
                    return true;
                return false;
            case GTHAN:
                if (val1 > val2)
                    return true;
                return false;
            case LTHAN:
                if (val1 < val2)
                    return true;
                return false;
            default:
                return false;
        }
    }
    private int comparison() {
        /** BNF: eq_v -> EQUALS | GREATER THAN | LESS THAN */
        currStatement.add(nextLex);
        int ret = nextTok;
        switch (nextTok){
            case EQUATOO:
                scan();
                break;
            case LTHAN:
                scan();
                break;
            case GTHAN:
                scan();
                break;
            default:
                error("Invalid comparison operator", scanner.getRow(), false);
                scan();
        }
        return ret;
    }
    private void optionalElse(boolean dontExecute) {
        /** opt_else ->
         | ELSE pactions */
        if (dontExecute)
            running = false;
        if(!dontExecute)
            running = true;
        //optionalElse method looking for end of if statement
        while (nextTok != ENDIF){
            actions();
        }
    }

    private void endfun() {
        /* BNF: ENDFUN IDENTIFIER */
        if (nextTok == IDENT || nextTok == MAIN){
            currStatement.add(nextLex);
            flush();
            scan();
        }
        else {
            error("Invalid function name", scanner.getRow(), true);
        }
    }
}
