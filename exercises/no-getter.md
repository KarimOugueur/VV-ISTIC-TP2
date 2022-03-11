# No getter!

With the help of JavaParser implement a program that obtains the private fields of public classes that have no public getter in a Java project. 

A field has a public getter if, in the same class, there is a public method that simply returns the value of the field and whose name is `get<name-of-the-field>`.

For example, in the following class:

```Java

class Person {
    private int age;
    private String name;
    
    public String getName() { return name; }

    public boolean isAdult() {
        return age > 17;
    }
}
```

`name` has a public getter, while `age` doesn't.

The program should take as input the path to the source code of the project. It should produce a report in the format of your choice (TXT, CSV, Markdown, HTML, etc.) that lists for each detected field: its name, the name of the declaring class and the package of the declaring class.

Include in this repository the code of your application. Remove all unnecessary files like compiled binaries. See the [instructions](../sujet.md) for suggestions on the projects to use.

*Disclaimer* In a real project not all fields need to be accessed with a public getter.



# Answer 


Pour cette partie de parserJava, nous avons utulisés le projet existant "javaparser-starter". 

Dans un premier temps, nous avoons modifié le code afin de passer en paramètre du visitor une map<String, Pair> et certaines methodes afin de récupérer l'ensemble des champs sans getter, leurs classes, et enfin leurs package. 
Dans la map, nous avons comme clé le package, et une paire (classe, champs sans getter). 

```Java
public class PublicElementsPrinter extends VoidVisitorWithDefaults<Map<String, Pair<String, Set<String>>>> {}

```
Nous avons créé une methode qui nous eprmet d'exporter le résultat en CSV. 


```Java
package fr.istic.vv;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import com.github.javaparser.utils.Pair;

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class PublicElementsPrinter extends VoidVisitorWithDefaults<Map<String, Pair<String, Set<String>>>> {

    File csvOutputFile = new File("test.csv");
    
    private String currentCLass ="";
    private String currentPackage="";
    private Set<String> fields = new HashSet<String>(); 
    private Set<String> tmp = new HashSet<String>();
    private Set<String> methods = new HashSet<String>();
    List<String[]> dataLines = new ArrayList<>();
    @Override
    public void visit(CompilationUnit unit, Map<String, Pair<String, Set<String>>> arg) {

        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, arg);
        }
        unit.getPackageDeclaration().ifPresent(d -> d.accept(this, arg));
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Map<String, Pair<String, Set<String>>> arg) {
        if(!declaration.isPublic()) return;
      //  System.out.println(declaration.getFullyQualifiedName().orElse("[Anonymous]"));
        for(MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }
        // retrieve all fields
        for(FieldDeclaration field : declaration.getFields()){
            field.accept(this, arg); 
        }
        // Printing nested types in the top level
        for(BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof TypeDeclaration)
                member.accept(this, arg);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Map<String, Pair<String, Set<String>>> arg) {
        currentCLass = declaration.getNameAsString(); 
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(EnumDeclaration declaration, Map<String, Pair<String, Set<String>>> arg) {
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(FieldDeclaration field, Map<String, Pair<String, Set<String>>> arg) {
        
        if (field.isPrivate()){

            String f = field.getVariables().get(0).getNameAsString();
            String capitalizedField = f.substring(0, 1).toUpperCase() + f.substring(1);
            fields.add(capitalizedField);
        }

    }

   @Override
   public void visit(MethodDeclaration declaration, Map<String, Pair<String, Set<String>>> arg) {
       if(!declaration.isPublic()) return;

       if(declaration.getNameAsString().contains("get")){
           methods.add(declaration.getNameAsString());
       }
   }

    @Override
    public void visit(PackageDeclaration pack, Map<String, Pair<String, Set<String>>> arg) {
        currentPackage = pack.getNameAsString(); 
        Set<String> tmpFields = new HashSet<>();
        Set<String> tmp2 = new HashSet<>();
      
        tmpFields.addAll(fields); 
    
        // vérifier si il y a une method get (get+Fields)
        for (String f : tmpFields) {
            String getField = "get"+f;
 
            if(methods.contains(getField)){
                tmp.add(f);
            }  
        }

        tmp2.addAll(tmpFields);
        // suppriemr les champs qui ont des getters 
        tmp2.removeAll(tmp);

        //if(!tmp2.isEmpty()){
        arg.put(currentPackage, new Pair<String,Set<String>>(currentCLass, tmp2));
        //}
        // clear fields 
        fields.clear();
    
        // write csv file 
       try {
            exportCSV(arg);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * formatting a single line of data represented as an array of Strings 
     * @param data
     * @return
     */

    public String convertToCSV(String[] data) {
        return Stream.of(data).collect(Collectors.joining(", "));
    }
    /**
     * let's convert each row with convertToCSV, and write it to a file
     * @param arg
     * @throws IOException
     */

    public void exportCSV(Map<String, Pair<String, Set<String>>> arg) throws IOException {
       
        for ( String p : arg.keySet()){
            if(!arg.get(p).b.isEmpty()){
                dataLines.add(new String[] { "Package : " + p , " Class : "+ arg.get(p).a.toString(),  "Fields Without Getter : " + arg.get(p).b.toString() });
            }
                
        }
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
              .map(this::convertToCSV)
              .forEach(pw::println);
        }
    }
 
}

```


Nousa vons testé avec ce code :

Repo : https://github.com/apache/commons-collections/tree/master/src 

Ce test permet de génrer un ficher CSV. 

```Java
package fr.istic.vv;
import java.io.File;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;


public class Main {
    
    public static void main() throws Exception{

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

```