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
    private static final String INT_VAL = "(\\-?([0-9]+|[a-zA-Z]+[0-9_a-zA-Z]*))";
    private static final String INT_COMPARE = "((" + INT_VAL + "\\s*==\\s*" + INT_VAL + ")|(" + INT_VAL + "\\s*!=\\s*" + INT_VAL +")|("
    + INT_VAL + "\\s*>=\\s*" + INT_VAL + ")|(" + INT_VAL + "\\s*<=\\s*" + INT_VAL + ")|(" + INT_VAL + "\\s*<\\s*" + INT_VAL + ")|("
    + INT_VAL + "\\s*>\\s*" + INT_VAL + "))";
    private static final String BOOL_COMPARE = "((" + BOOL_VAL + "\\s*==\\s*" + BOOL_VAL + ")|(" + BOOL_VAL + "\\s*!=\\s*" + BOOL_VAL +"))";
    private static final String INT_EXPR = "((" + INT_VAL + "\\s*\\+\\s*)|(" + INT_VAL +"\\s*\\-\\s*)|(" +
    INT_VAL +"\\s*\\*\\s*)|(" + INT_VAL +"\\s*/\\s*)|(" + INT_VAL + "\\s*%\\s*))*" + INT_VAL;
    private static final String BOOL_EXPR = "((((not\\s+)?" + BOOL_VAL + "\\s+or\\s+)|((not\\s+)?" + BOOL_VAL + "\\s+and\\s+))*(not\\s+)?" + BOOL_VAL
    + ")|(" + INT_COMPARE + ")|(" + BOOL_COMPARE + "))";
    private static final String INT_ASSIGN = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + INT_EXPR + ")";
    private static final String BOOL_ASSIGN = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + BOOL_EXPR + ")";
    private static Pattern intDecl = Pattern.compile("int\\s+" + INT_ASSIGN + "\\."); 
    private static Pattern boolDecl = Pattern.compile("bool\\s+" + BOOL_ASSIGN + "\\.");
    private static Pattern intAssgmt = Pattern.compile(INT_ASSIGN + "\\.");
    private static Pattern boolAssgmt = Pattern.compile(BOOL_ASSIGN + "\\.");
    private static Pattern print = Pattern.compile("print\\((.*)\\)\\.");
    private static Pattern boolExpr = Pattern.compile(BOOL_EXPR);
    private static Pattern intExpr = Pattern.compile(INT_EXPR);
    private static Pattern whileCond = Pattern.compile("while\\s+" + BOOL_EXPR);
    private static Pattern ifCond = Pattern.compile("if\\s+" + BOOL_EXPR);
    private static Pattern elfCond = Pattern.compile("elf\\s+" + BOOL_EXPR);

    public static void main(String[] args){
        if(args.length == 0){
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
        File file = new File(baseName + ".java");
        try {
            Scanner input = new Scanner(new File(pathName));
            file.delete();
            file.createNewFile();
            FileWriter output = new FileWriter(file);
            output.write("public class " + className + " {\n");
            output.write("public static void main(String[] ARGS) {\n");
            while (input.hasNextLine()){
                String line = input.nextLine().strip();
                if(line.equals(""))
                  continue;
                
                // int declaration
                matcher = intDecl.matcher(line);
                if (matcher.find()) {
                    output.write("int "+ matcher.group(1) + " = " + matcher.group(2) + ";\n");
                    continue;
                }
                if(line.startsWith("fin") && blockTracker.isEmpty()){
                    System.out.println("ERROR: You added a 'fin' before a conditional or loop!");
                    output.close();
                    input.close();
                    System.exit(1);
                }

                // boolean declaration
                matcher = boolDecl.matcher(line);
                if (matcher.find()) {
                    output.write("boolean "+ matcher.group(1) + " = " + matcher.group(2) + ";\n");
                    continue;
                }

                // int assignment
                matcher = intAssgmt.matcher(line);
                if (matcher.find()) {
                    output.write(matcher.group(1) + " = " + matcher.group(2) + ";\n");
                    continue;
                }

                // boolean assignment
                matcher = boolAssgmt.matcher(line);
                if (matcher.find()) {
                    output.write(matcher.group(1) + " = " + matcher.group(2) + ";\n");
                    continue;
                }

                // print
                matcher = print.matcher(line);
                if (matcher.find()) {
                    output.write("System.out.print(" + matcher.group(1) + ");\n");
                    continue;
                }

                System.out.println("Invalid syntax.");
                throw new Exception();
            }
            output.write("\n}\n}\n");
            output.close();
            input.close();
            if(!blockTracker.isEmpty()){
                System.out.println("ERROR: You forgot a 'fin' somewhere!");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }
}