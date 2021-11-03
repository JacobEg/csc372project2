/*

*/

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;

public class Translator{
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
        Scanner input = new Scanner(new File(pathName));
        String baseName = pathName.substring(0, pathName.length()-4);
        String className = getClassName(baseName);
        File file = new File(baseName + ".java");
        file.createNewFile();
        FileWriter output = new FileWriter(file);
        output.write("public class " + className + " {\n");
        output.write("public static void main(String[] ARGS) {\n");
        while (input.hasNextLine()){
            // TODO
        }
        output.write("}\n}\n");
        output.close();
        input.close();
    }
}