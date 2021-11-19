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

public class Translator{
    // maps vars to their type (str, int, bool)
    private static HashMap<String, String> vars = new HashMap<String, String>();
    private static Matcher matcher;
    // look into expressions; recursive regex?
    private static final String BOOL_VAL = "(true|false|[a-zA-Z]+[0-9_a-zA-Z]*)";
    private static final String INT_VAL = "(\\-?([0-9]+|[a-zA-Z]+[0-9_a-zA-Z]*))";
    private static Pattern varDecl = Pattern.compile("int\\s+[a-zA-Z]+[0-9_a-zA-Z]*\\s*=\\s*" + INT_VAL + "\\." + 
    "|bool\\s+[a-zA-Z]+[0-9_a-zA-Z]*\\s*=\\s*" + BOOL_VAL + "\\.");
    private static Pattern varAssgmt = Pattern.compile("[a-zA-Z]+[0-9_a-zA-Z]*\\s*=\\s*(\\-?[0-9]+|true|false|\\-?[a-zA-Z]+[0-9_a-zA-Z]*)\\.");
    private static Pattern print = Pattern.compile("print\\((\".*\"|'.*')\\)\\.|print\\([a-zA-Z]+[0-9_]*\\)\\.");
    private static Pattern boolExpr = Pattern.compile("((((not)\\s+)?" + BOOL_VAL + "\\s+or\\s+)|(((not)\\s+)?" + BOOL_VAL + "\\s+and\\s+))*" + BOOL_VAL);
    private static Pattern intExpr = Pattern.compile("((" + INT_VAL + "\\s*\\+\\s*)|(" + INT_VAL +"\\s*\\-\\s*)|(" +
    INT_VAL +"\\s*\\*\\s*)|(" + INT_VAL +"\\s*/\\s*)|(" + INT_VAL + "\\s*%\\s*))*" + INT_VAL);

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
            file.createNewFile();
            FileWriter output = new FileWriter(file);
            output.write("public class " + className + " {\n");
            output.write("public static void main(String[] ARGS) {\n");
            while (input.hasNextLine()){
                String line = input.nextLine().strip();
                // TODO: analyze line-by-line (how to do conditionals and loops)?
                matcher = varAssgmt.matcher(line);
                if (matcher.find())
                    System.out.printf("Variable %s was assigned the value %s", matcher.group(1), matcher.group(2));
            }
            output.write("\n}\n}\n");
            output.close();
            input.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}