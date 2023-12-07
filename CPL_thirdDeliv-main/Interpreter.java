package pkg;

public class Interpreter {
    public static void main(String[] args) {
        //file name is passed in as argument

        if (args.length > 1) {
            switch (args[0]) {
                case "-p":
                    System.out.printf("\nNow parsing file %s \n\n", args[1]);
                    Parser p = new Parser(args[1]);
                    //executes until end of file is reached
                    while (!p.endOfFileReached()) {
                        // parseProgram parses for 1 complete function at a time
                        p.parseProgram();


                    }
                    System.out.println("\nEnd of file reached.");
                    break;
                case "-s":
                    System.out.printf("\nNow Scanning file %s \n\n", args[1]);
                    Scanner s = new Scanner(new java.io.File(args[1]));
                    while (!s.endOfFileReached()) {      //executes until end of file is reached

                        //next token is converted to a string by convertType function
                        System.out.printf("%s, row %d, col %d, symbol: %s\n",
                                s.convertType(s.nextToken()), s.getRow(), s.getCol(), s.currentLexeme);
                    }
                    System.out.println("\nEnd of file reached.");
                    break;
                //executes until end of file is reached
                case "-e":
                    System.out.printf("\nNow Executing file %s \n\n", args[1]);
                    Parser p2 = new Parser(args[1]);
                    p2.running = true;
                    // parser both parses line by line and then executes corresponding function
                    while (!p2.endOfFileReached()) {
                        p2.parseProgram();


                    }
                    break;
            }
        } else {
            System.out.printf("\nNow Executing file: %s \n\n", args[0]);
            Parser p2 = new Parser(args[0]);
            p2.running = true;
            //executes until end of file is reached
            while (!p2.endOfFileReached()) {
                // parser both parses line by line and then executes corresponding function
                p2.parseProgram();


            }
        }

    }
}
