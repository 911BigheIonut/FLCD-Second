package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Scanner {
    private String program;
    private final List<String> tokens;
    private final List<String> reservedWords;

    private SymbolTable symbolTable;

    //                     token         hashCodifi, posilistfrhc
    private List<KeyValue<String, KeyValue<Integer, Integer>>> PIF;
    private int index = 0;
    private int currentLine = 1;

    public Scanner() {
        this.symbolTable = new SymbolTable(47);
        this.PIF = new ArrayList<>();
        this.reservedWords = new ArrayList<>();
        this.tokens = new ArrayList<>();
        try {
            readTokensFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setProgram(String program) {
        this.program = program;
    }

    private void readTokensFromFile() throws IOException {
        File file = new File("src/utils/token.in");
        BufferedReader br = Files.newBufferedReader(file.toPath());
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(" ");
            switch (parts[0]) {
                case "integer", "char", "string", "for", "to", "do", "endfor", "while", "endwhile", "if", "else", "then", "endif", "write", "read", "var", "startprogram", "endprogram", "and" -> reservedWords.add(parts[0]);
                default -> tokens.add(parts[0]);
            }
        }
    }




    private void ignoreComments() {
        while (index < program.length() && program.charAt(index) == '#') {
            while (index < program.length() && program.charAt(index) != '\n') {
                index++;
            }
        }
    }


    private void ignoreSpaces() {
        while (index < program.length() && Character.isWhitespace(program.charAt(index))) {
            if (program.charAt(index) == '\n') {
                currentLine++;
            }
            index++;
        }
    }


    private boolean treatStringConstant() {
        var regexForStringConstant = Pattern.compile("^\"[a-zA-z0-9_ ?:*^+=.!]*\"");
        var matcher = regexForStringConstant.matcher(program.substring(index));
        if (!matcher.find()) {
            if (Pattern.compile("^\"[^\"]\"").matcher(program.substring(index)).find()) {
                throw new ScanException("Invalid string constant at line " + currentLine);
            }
            if (Pattern.compile("^\"[^\"]").matcher(program.substring(index)).find()) {
                throw new ScanException("Missing \" at line " + currentLine); // it's like "a"
            }
            return false;
        }
        var stringConstant = matcher.group(0);
        index += stringConstant.length();
        KeyValue<Integer, Integer> position;
        try {
            position = symbolTable.addStringConstant(stringConstant);
        } catch (Exception e) {
            position = symbolTable.getPositionStringConstant(stringConstant);
        }
        PIF.add(new KeyValue<>("str const", position));
        return true;
    }




    private boolean checkIfValid(String possibleIdentifier, String programSubstring) {
        if (reservedWords.contains(possibleIdentifier)) {
            return false;
        }
        if (Pattern.compile("^[A-Za-z_][A-Za-z]*").matcher(programSubstring).find()) {
            return true;
        }
        return symbolTable.hasIdentifier(possibleIdentifier);
    }


    private boolean treatIdentifier() {
        // Create a regex pattern to match variable declarations
       var regexForVariableDeclaration = Pattern.compile("^var (integer|string|char) ([a-zA-Z_][a-zA-Z_\\d]*(, [a-zA-Z_][a-zA-Z_\\d]*)*);");
        //var regexForVariableDeclaration = Pattern.compile("^([a-zA-Z_][a-zA-Z_\\d]*(, [a-zA-Z_][a-zA-Z_\\d]*)*);");
        //var regexForVariableDeclaration = Pattern.compile("^var (integer|string|char) ([a-zA-Z_][a-zA-Z_]*(, [a-zA-Z_][a-zA-Z_]*)*);");
        //var regexForVariableDeclaration = Pattern.compile("^var (integer|string|char) ([a-zA-Z_][a-zA-Z_]+(, [a-zA-Z_][a-zA-Z_]+)+);");
        var matcher = regexForVariableDeclaration.matcher(program.substring(index));

        if (matcher.find()) {
            // Extract the variable type
            String variableType = matcher.group(1);

            // Extract the list of variable names
            String variableNames = matcher.group(2);

            // Split the variable names into an array
            String[] variableNameArray = variableNames.split(", ");

            // Process and add each variable to your symbol table
            for (String variableName : variableNameArray) {
                if (!checkIfValid(variableName, program.substring(index))) {
                    return false;
                }

                // Add the variable to the symbol table
                KeyValue<Integer, Integer> position;
                try {
                    position = symbolTable.addIdentifier(variableName);
                } catch (Exception e) {
                    position = symbolTable.getPositionIdentifier(variableName);
                }

                // Add the variable to PIF with its type
                PIF.add(new KeyValue<>(variableType, position));
            }

            // Update the index to skip the processed part
            index += matcher.group().length();

            return true;
        }

        return false;
    }


//    private boolean treatIntConstant(){
//        var regexForIntConstant = Pattern.compile("^([+-]?[1-9][0-9]*|0)");
//        var matcher = regexForIntConstant.matcher(program.substring(index));
//        if (!matcher.find()) {
//            return false;
//        }
//        if (Pattern.compile("^([+-]?[1-9][0-9]*|0)[a-zA-z_]").matcher(program.substring(index)).find()) {
//            return false;
//        }
//        var intConstant = matcher.group(1);
//        index += intConstant.length();
//        KeyValue<Integer, Integer> position;
//        try {
//            position = symbolTable.addIntConstant(Integer.parseInt(intConstant));
//        } catch (Exception e) {
//            position = symbolTable.getPositionIntConstant(Integer.parseInt(intConstant));
//        }
//        PIF.add(new KeyValue<>("int const", position));
//        return true;
//    }
//
//
//    private boolean treatFromTokenList() {
//        String possibleToken = program.substring(index).split(" ")[0];
//        for (var reservedToken: reservedWords) {
//            if (possibleToken.startsWith(reservedToken)) {
//                var regex = "^" + "[a-zA-Z0-9_]*" + reservedToken + "[a-zA-Z0-9_]+";
//                if (Pattern.compile(regex).matcher(possibleToken).find()) {
//                    return false;
//                }
//                index += reservedToken.length();
//                PIF.add(new KeyValue<>(reservedToken, new KeyValue<>(-1, -1)));
//                return true;
//            }
//        }
//        for (var token : tokens) {
//            if (Objects.equals(token, possibleToken)) {
//                index += token.length();
//                PIF.add(new KeyValue<>(token, new KeyValue<>(-1, -1)));
//                return true;
//            }
//            else if (possibleToken.startsWith(token)) {
//                index += token.length();
//                PIF.add(new KeyValue<>(token, new KeyValue<>(-1, -1)));
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//
//
//
//    private void nextToken() throws ScanException{
//        ignoreSpaces();
//        ignoreComments();
//        if (index == program.length()) {
//            return;
//        }
//        if (treatIdentifier()) {
//            return;
//        }
//        if (treatStringConstant()) {
//            return;
//        }
//        if (treatIntConstant()) {
//            return;
//        }
//        if (treatFromTokenList()) {
//            return;
//        }
//        throw new ScanException("Lexical error: invalid token at line " + currentLine + ", index " + index);
//    }

    /////aiciii schimb acuma fix ce e comentat sub e bunicel



    private boolean treatFromTokenList() {
        // Split the program string by whitespace or newline
        String[] tokensInProgram = program.substring(index).split("[\\s\\n]+");

        // Check the first token
        String possibleToken = tokensInProgram[0];



        for (var reservedToken : reservedWords) {
            if (possibleToken.equals(reservedToken)) {
                index += possibleToken.length();
                PIF.add(new KeyValue<>(reservedToken, new KeyValue<>(-1, -1)));
                return true;
            }
        }

        for (var token : tokens) {
            if (Objects.equals(token, possibleToken)) {
                index += possibleToken.length();
                PIF.add(new KeyValue<>(token, new KeyValue<>(-1, -1)));
                return true;
            } else if (possibleToken.startsWith(token)) {
                index += token.length();
                PIF.add(new KeyValue<>(token, new KeyValue<>(-1, -1)));
                return true;
            }
        }


        // Check if the token is a valid identifier
        if (Pattern.compile("^[A-Za-z_][A-Za-z_\\d]*$").matcher(possibleToken).find()) {
            index += possibleToken.length();
            PIF.add(new KeyValue<>("identifier", new KeyValue<>(-1, -1)));
            return true;
        }
        // If no token was recognized, return false
        return false;
    }








    private boolean treatIntConstant() {
        var regexForIntConstant = Pattern.compile("^[+-]?[1-9][0-9]*|0(?![0-9])");
        var matcher = regexForIntConstant.matcher(program.substring(index));
        if (!matcher.find()) {
            return false;
        }
        var intConstant = matcher.group();
        index += intConstant.length();
        KeyValue<Integer, Integer> position;
        try {
            position = symbolTable.addIntConstant(Integer.parseInt(intConstant));
        } catch (Exception e) {
            position = symbolTable.getPositionIntConstant(Integer.parseInt(intConstant));
        }
        PIF.add(new KeyValue<>("int const", position));
        return true;
    }




    private void nextToken() throws ScanException {
        ignoreSpaces();
        ignoreComments();
        if (index == program.length()) {
            return;
        }
        if (treatIdentifier()) {
            return;
        }
        if (treatStringConstant()) {
            return;
        }
        if (treatIntConstant()) {
            return;
        }
        if (treatFromTokenList()) {
            return;
        }
        throw new ScanException("Lexical error: invalid token at line " + currentLine + ", index " + index);
    }



    public void scan(String programFileName){
        try {
            Path file = Path.of("src/utils/" + programFileName);
            setProgram(Files.readString(file));
            index = 0;
            PIF = new ArrayList<>();
            symbolTable = new SymbolTable(47);
            currentLine = 1;
            while (index < program.length()) {
                nextToken();
            }
            FileWriter fileWriter = new FileWriter("PIF" + programFileName.replace(".txt", ".out"));
            for (var pair : PIF) {
                fileWriter.write(pair.getKey() + " -> (" + pair.getValue().getKey() + ", " + pair.getValue().getValue() + ")\n");
            }
            fileWriter.close();
            fileWriter = new FileWriter("ST" + programFileName.replace(".txt", ".out"));
            fileWriter.write(symbolTable.toString());
            fileWriter.close();
            System.out.println("Lexically correct");
        } catch (IOException | ScanException e) {
            System.out.println(e.getMessage());
        }
    }
}
