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
    private static String boolVal = "(true|false)";
    private static String intVal = "(\\-?([0-9]+|ARGS\\[[0-9]+\\]))";
    private static String intExpr = "((" + intVal + "\\s*(\\+|\\-|\\*|/|%)\\s*)*" + intVal + ")";
    private static String intCompare = "(" + intExpr + "\\s*(==|!=|>=|<=|<|>)\\s*" + intExpr + ")";
    private static String boolCompare = "(" + boolVal + "\\s*(==|!=)\\s*" + boolVal + ")";
    private static String boolReturned = "(" + intCompare + "|" + boolCompare + "|" + boolVal + ")";
    private static String boolExpr = "(((not\\s+)?" + boolReturned + "\\s+(or|and)\\s+)*(not\\s+)?" + boolReturned + ")";
    private static String intAssign = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + intExpr + ")";
    private static String boolAssign = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + boolExpr + ")";
    private static Pattern intDecl = Pattern.compile("int\\s+" + intAssign + "\\."); 
    private static Pattern boolDecl = Pattern.compile("bool\\s+" + boolAssign + "\\.");
    private static Pattern intAssgmt = Pattern.compile(intAssign + "\\.");
    private static Pattern boolAssgmt = Pattern.compile(boolAssign + "\\.");
    private static Pattern print = Pattern.compile("print\\((\".*\"|" + boolExpr + "|" + intExpr + ")\\)\\.");
    private static Pattern whileCond = Pattern.compile("while\\s+(" + boolExpr + ")");
    private static Pattern ifCond = Pattern.compile("if\\s+(" + boolExpr + ")");
    private static Pattern elfCond = Pattern.compile("elf\\s+(" + boolExpr + ")");

    public static void main(String[] args){
        if(args.length == 0) {
            System.out.println("Provide file to translate via command-line arg.");
            return;
        }
        translate(args[0]);
    }

    public static void recompile(){
        intExpr = "((" + intVal + "\\s*(\\+|\\-|\\*|/|%)\\s*)*" + intVal + ")";
        intCompare = "(" + intExpr + "\\s*(==|!=|>=|<=|<|>)\\s*" + intExpr + ")";
        boolCompare = "(" + boolVal + "\\s*(==|!=)\\s*" + boolVal + ")";
        boolReturned = "(" + intCompare + "|" + boolCompare + "|" + boolVal + ")";
        boolExpr = "(((not\\s+)?" + boolReturned + "\\s+(or|and)\\s+)*(not\\s+)?" + boolReturned + ")";
        intAssign = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + intExpr + ")";
        boolAssign = "([a-zA-Z]+[0-9_a-zA-Z]*)\\s*=\\s*(" + boolExpr + ")";
        intDecl = Pattern.compile("int\\s+" + intAssign + "\\."); 
        boolDecl = Pattern.compile("bool\\s+" + boolAssign + "\\.");
        intAssgmt = Pattern.compile(intAssign + "\\.");
        boolAssgmt = Pattern.compile(boolAssign + "\\.");
        print = Pattern.compile("print\\((\".*\"|" + boolExpr + "|" + intExpr + ")\\)\\.");
        whileCond = Pattern.compile("while\\s+(" + boolExpr + ")");
        ifCond = Pattern.compile("if\\s+(" + boolExpr + ")");
        elfCond = Pattern.compile("elf\\s+(" + boolExpr + ")");
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

    public static int count(String superstr, String substr){
        int count = 0;
        for(int i = 0; i < superstr.length()-substr.length()+1; i++){
            if(substr.equals(superstr.substring(i, i+substr.length()))){
                count++;
            }
        }
        return count;
    }

    public static String translateBoolExpr(String boolExpr){
        String ret = boolExpr.replaceAll("\\sand\\s", " && ").replaceAll("\\sor\\s", " || ").replaceAll("\\snot\\s", " !(");
        int count = count(ret, "!");
        for(int i = 0; i < count; i++){
            ret += ")";
        }
        return ret;
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
                if(line.equals("") || line.startsWith("#"))
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
                    // add var to intval regex
                    intVal = intVal.substring(0, intVal.length()-2) + "|" + matcher.group(1) +"))";
                    recompile();
                    continue;
                }
                
                // boolean declaration
                matcher = boolDecl.matcher(line);
                if (matcher.find()) {
                    output += "boolean "+ matcher.group(1) + " = " + translateBoolExpr(matcher.group(2)) + ";\n";
                    // check if variable already exists
                    if (vars.containsKey(matcher.group(1))) {
                        System.out.println("Error: Variable with name " + matcher.group(1) + " already declared.");
                        input.close();
                        System.exit(1);
                    }
                    vars.put(matcher.group(1), "bool");
                    // add var to boolval regex
                    boolVal = boolVal.substring(0, boolVal.length()-1) + "|" + matcher.group(1) +")";
                    recompile();
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
                    output += matcher.group(1) + " = " + translateBoolExpr(matcher.group(2)) + ";\n";
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
                    output += "while(" + translateBoolExpr(matcher.group(1)) + "){\n";
                    continue;
                }

                // if-statement
                matcher = ifCond.matcher(line);
                if (matcher.find()) {
                    blockTracker.push('i');
                    String boolEx = matcher.group(1);
                    output += "if (" + translateBoolExpr(boolEx) + ") {\n";
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
                        output += "}else if (" + translateBoolExpr(boolEx) + ") {\n";
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
                        blockTracker.pop();
                        blockTracker.push('e');
                    } else{
                        System.out.println("Error: unexpected else!");
                        input.close();
                        System.exit(1);
                    }
                    continue;
                }

                // line didn't match any regexes
                System.out.println("Error on line: " + line);
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