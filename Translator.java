/*
Authors: Jacob Egestad & Cade Marks
File: Translator.java
Project: Project 2
Description: Translates a .txt file in our programming language from the command line to java
*/

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.*;
import java.util.HashMap;
import java.util.Stack;

public class Translator{
    // maps vars to their type (str, int, bool)
    private static Stack<Character> blockTracker = new Stack<Character>();
    private static HashMap<String, String> vars = new HashMap<String, String>();
    private static Matcher matcher;
    // look into expressions; recursive regex?
    private static final String BOOL_VAL = "(true|false|[a-zA-Z]+[0-9_a-zA-Z]*)";
    private static final String INT_VAL = "(\\-?([0-9]+|ARGS\\[[0-9]+\\]|[a-zA-Z]+[0-9_a-zA-Z]*))";
    private static final String INT_EXPR = "((" + INT_VAL + "\\s*(\\+|\\-|\\*|/|%)\\s*)*" + INT_VAL + ")";
    private static final String INT_COMPARE = "(" + INT_EXPR + "\\s*(==|!=|>=|<=|<|>)\\s*" + INT_EXPR + ")";
    private static final String BOOL_COMPARE = "(" + BOOL_VAL + "\\s*(==|!=)\\s*" + BOOL_VAL + ")";
    private static final String BOOL_RETURNED = "(" + INT_COMPARE + "|" + BOOL_COMPARE + "|" + BOOL_VAL + ")";
    private static final String BOOL_EXPR = "(((not\\s+)?" + BOOL_RETURNED + "\\s+(or|and)\\s+)*(not\\s+)?" + BOOL_RETURNED + ")";
    private static final String INT_ASSIGN = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + INT_EXPR + ")";
    private static final String BOOL_ASSIGN = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + BOOL_EXPR + ")";
    private static Pattern intDecl = Pattern.compile("int\\s+" + INT_ASSIGN + "\\."); 
    private static Pattern boolDecl = Pattern.compile("bool\\s+" + BOOL_ASSIGN + "\\.");
    private static Pattern intAssgmt = Pattern.compile(INT_ASSIGN + "\\.");
    private static Pattern boolAssgmt = Pattern.compile(BOOL_ASSIGN + "\\.");
    private static Pattern print = Pattern.compile("print\\((\".*\"|" + BOOL_EXPR + "|" + INT_EXPR + ")\\)\\.");
    //private static Pattern boolExpr = Pattern.compile(BOOL_EXPR);
    //private static Pattern intExpr = Pattern.compile(INT_EXPR);
    private static Pattern whileCond = Pattern.compile("while\\s+(" + BOOL_EXPR + ")");
    private static Pattern ifCond = Pattern.compile("if\\s+(" + BOOL_EXPR + ")");
    private static Pattern elfCond = Pattern.compile("elf\\s+(" + BOOL_EXPR + ")");

    public static void main(String[] args){
        if(args.length == 0) {
            System.out.println("Provide file to translate via command-line arg.");
            return;
        }
        translate(args[0]);
    }

    public static String getClassName(String baseName){
        int slashIndex = baseName.lastIndexOf('/');
        int backSlashIndex = baseName.lastIndexOf('\\');
        if(slashIndex != -1){
            return baseName.substring(slashIndex + 1);
        } else if (backSlashIndex != -1){
            return baseName.substring(backSlashIndex + 1);
        } else{
            return baseName;
        }
    }

    public static void translate(String pathName){
        String baseName = pathName.substring(0, pathName.length()-4);
        String className = getClassName(baseName);
        try {
            Scanner input = new Scanner(new File(pathName));
            String output = "";
            output += "public class " + className + " {\n";
            output +="public static void main(String[] fin) {\n";
            output += "int[] ARGS = new int[fin.length];\n";
            output += "for(int i = 0; i < fin.length; i++){\n";
            output += "ARGS[i] = Integer.parseInt(fin[i]);\n}\n";
            while (input.hasNextLine()){
                String line = input.nextLine().strip();
                if(line.equals(""))
                  continue;
                
                if(line.startsWith("fin")){
                    if(blockTracker.isEmpty()){
                        System.out.println("ERROR: You added a 'fin' before a conditional or loop!");
                        input.close();
                        System.exit(1);
                    } else{
                        blockTracker.pop();
                        output += "}\n";
                    }
                    continue;
                }

                // int declaration
                matcher = intDecl.matcher(line);
                if (matcher.find()) {
                    output +="int "+ matcher.group(1) + " = " + matcher.group(2) + ";\n";
                    // check if variable already exists
                    if (vars.containsKey(matcher.group(1))) {
                        System.out.println("Error: Variable with name " + matcher.group(1) + " already declared.");
                        input.close();
                        System.exit(1);
                    }
                    vars.put(matcher.group(1), "int");
                    continue;
                }
                
                // boolean declaration
                matcher = boolDecl.matcher(line);
                if (matcher.find()) {
                    output += "boolean "+ matcher.group(1) + " = " + matcher.group(2) + ";\n";
                    // check if variable already exists
                    if (vars.containsKey(matcher.group(1))) {
                        System.out.println("Error: Variable with name " + matcher.group(1) + " already declared.");
                        input.close();
                        System.exit(1);
                    }
                    vars.put(matcher.group(1), "bool");
                    continue;
                }

                // int assignment
                matcher = intAssgmt.matcher(line);
                if (matcher.find()) {
                    // check if variable already exists
                    if (!vars.containsKey(matcher.group(1))) {
                        System.out.println("Error: Variable " + matcher.group(1) + " not found.");
                        input.close();
                        System.exit(1);
                    }
                    // check if variable is of correct type
                    if (vars.get(matcher.group(1)).equals("int")) {
                        output += matcher.group(1) + " = " + matcher.group(2) + ";\n";
                        continue;
                    }
                }

                // boolean assignment
                matcher = boolAssgmt.matcher(line);
                if (matcher.find()) {
                    // check if variable already exists
                    if (!vars.containsKey(matcher.group(1))) {
                        System.out.println("Error: Variable " + matcher.group(1) + " not found.");
                        input.close();
                        System.exit(1);
                    }
                    // check if variable is of correct type
                    if (!vars.get(matcher.group(1)).equals("bool")) {
                        System.out.println("Type mismatch: " + matcher.group(1) + " is not a boolean");
                        input.close();
                        System.exit(1);
                    }
                    output += matcher.group(1) + " = " + matcher.group(2) + ";\n";
                    continue;
                }

                // print
                matcher = print.matcher(line);
                if (matcher.find()) {
                    output += "System.out.print(" + matcher.group(1) + ");\n";
                    continue;
                }

                // while
                matcher = whileCond.matcher(line);
                if(matcher.find()){
                    blockTracker.push('w');
                    output += "while(" +
                    matcher.group(1).replaceAll("\\s+and\\s+", " && ").replaceAll("\\s+or\\s+", " || ").replaceAll("not\\s+", "!") +
                    "){\n";
                    continue;
                }

                // if-statement
                matcher = ifCond.matcher(line);
                if (matcher.find()) {
                    blockTracker.push('i');
                    String boolEx = matcher.group(1);
                    output += "if (" +
                    boolEx.replaceAll("\\s+and\\s+", " && ").replaceAll("\\s+or\\s+", " || ").replaceAll("not\\s+", "!") +
                    ") {\n";
                    if(!input.hasNextLine()){
                        System.out.println("Error: ended file with if statement");
                        input.close();
                        System.exit(1);
                    }
                    line = input.nextLine().strip();
                    if(!line.startsWith("then")){
                        System.out.println("Error: missing 'then' after if statement");
                        input.close();
                        System.exit(1);
                    }
                    continue;
                }

                // elf
                matcher = elfCond.matcher(line);
                if(matcher.find()){
                    if(!blockTracker.isEmpty() && blockTracker.peek() == 'i'){
                        String boolEx = matcher.group(1);
                        output += "}else if (" +
                        boolEx.replaceAll("\\s+and\\s+", " && ").replaceAll("\\s+or\\s+", " || ").replaceAll("not\\s+", "!") +
                        ") {\n";
                        if(!input.hasNextLine()){
                            System.out.println("Error: ended file with if statement");
                            input.close();
                            System.exit(1);
                        }
                        line = input.nextLine().strip();
                        if(!line.startsWith("then")){
                            System.out.println("Error: missing 'then' after if statement");
                            input.close();
                            System.exit(1);
                        }
                    } else{
                        System.out.println("Error: unexpected elf!");
                        input.close();
                        System.exit(1);
                    }
                    continue;
                }

                // else
                if(line.startsWith("else")){
                    if(!blockTracker.isEmpty() && blockTracker.peek() == 'i'){
                        output += "} else {\n";
                    } else{
                        System.out.println("Error: unexpected else!");
                        input.close();
                        System.exit(1);
                    }
                    continue;
                }

                // line didn't match any regexes
                System.out.println("Error: Invalid Syntax");
                System.out.println(" On line: " + line);
                input.close();
                System.exit(1);
            }
            
            output += "\n}\n}\n";
            input.close();

            if(!blockTracker.isEmpty()){
                System.out.println("ERROR: You forgot a 'fin' somewhere!");
                System.exit(1);
            }

            // write output string to new file
            File file = new File(baseName + ".java");
            file.delete();
            file.createNewFile();
            FileWriter fileOut = new FileWriter(file);
            fileOut.write(output);
            fileOut.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }
}