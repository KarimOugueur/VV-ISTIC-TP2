package fr.istic.vv;

import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String [] args) throws Exception{

        
        String pathDirectory= "/home/ougueur/Bureau/M2_ILAAAA/vv/commons-collections/src/main/java/org/apache/commons/collections4"; 
        //String pathDirectory="/home/ougueur/Bureau/M2_ILAAAA/vv/commons-collections/src/test/java/org/apache/commons/collections4";
        Map<String, Pair<String, Set<String>>> arg = new HashMap();

        File file = new File(pathDirectory);
        if(!file.exists() || !file.isDirectory() || !file.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }

        SourceRoot root = new SourceRoot(file.toPath());
        PublicElementsPrinter printer = new PublicElementsPrinter();

        
        root.parse("", (localPath, absolutePath, result) -> {   
            result.ifSuccessful(unit -> unit.accept(printer, arg));  
            return SourceRoot.Callback.Result.DONT_SAVE;
        });
        
    }

    
}
