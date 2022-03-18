# Class cohesion with JavaParser

With the help of JavaParser implement a program that computes the Tight Class Cohesion (TCC) for each class in a given Java project. The program should take as input the path to the source code of the project. It should produce a report in the format of your choice (TXT, CSV, Markdown, HTML, etc.) containing a table showing for each class: the package, name and TCC value. 
Your application should also produce a histogram showing the distribution of CC values in the project. Compare the histogram of two or more projects.
Finally, your application should also produce the dependency graph of each class (cf. example [here](https://people.irisa.fr/Benoit.Combemale/pub/course/vv/vv-textbook-v0.1.pdf#cohesion-graph)). The graph should be written using the [GraphViz DOT format](https://www.graphviz.org/)

Ignore inherited members to compute TCC of a class.

Include in this repository the code of your application. Remove all unnecessary files like compiled binaries. Do include the reports and plots you obtained from different projects. See the [instructions](../sujet.md) for suggestions on the projects to use.

You may use [javaparser-starter](../code/javaparser-starter) as a starting point.


# Answer

Dans cette partie de calcul de TCC. Nous avons calculé le TCC de chaque classe.

Dans un premier temps, nous récuperons les champs de chaque classe, ensuite on récupère l'ensemble des methodes qui utilise les attributs de cette dernière (classe en question). 
Dans un seconds temps, nous récupérons toutes les methodes qui sont connecté directement.

NP = le nombre maximal possible de paires des méthodes connectées
NDC = le nombre de paires des méthodes directement liées

TCC = NDC / NP 



```Java
package fr.istic.vv;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import com.github.javaparser.utils.Pair;

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class PublicElementsPrinter extends VoidVisitorWithDefaults<Void> {


    private String currentCLass ="";
    private String currentPackage="";
    private int numberOfNodeForCurrentClass = 0;

    private Set<VariableDeclarator> fields = new HashSet<VariableDeclarator>();
    private Map<String, Optional<BlockStmt>> methods = new HashMap<>();
    private Map<String, Pair<String, Integer>> packageClassField = new HashMap<>();
    private Map<String, Integer> classesWithNodes = new HashMap<>();
    

    //private Pair<String, Integer> classbyNP = new Pair<String, integer>();
    private Map<String, String> methodPairs = new HashMap();


    private Map<String, Pair<String, String>> methodconnected = new HashMap();
    public Map<String, Integer> getClassesWithNodes(){
        return classesWithNodes;
    }


    @Override
    public void visit(CompilationUnit unit, Void arg) {

        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, arg);
            currentCLass = type.getNameAsString();
            classesWithNodes.put(currentCLass, numberOfNodeForCurrentClass);
        }
       // classesWithNodes.put(unit.getPackageDeclaration().toString(), numberOfNodeForCurrentClass);
        numberOfNodeForCurrentClass = 0;

        unit.getPackageDeclaration().ifPresent(d -> d.accept(this, arg));
        //System.out.print( " TCC : " + TCC(methods.size()));
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
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
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
       // currentCLass = declaration.getNameAsString();
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(EnumDeclaration declaration, Void arg) {
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(FieldDeclaration field, Void arg) {
        fields.addAll(field.getVariables());
    }

    @Override
    public void visit(MethodDeclaration declaration,Void  arg) {

        numberOfNodeForCurrentClass++;

        // si le corp de la methode n'est pas vide ajoute
        // la méthode dans la liste de smethode
        for (VariableDeclarator f : fields){
            if(declaration.getBody().isPresent() && declaration.getBody().toString().contains(f.toString())){
                methods.put(declaration.getNameAsString(), declaration.getBody());
            }
        }
        
    }

    @Override
    public void visit(PackageDeclaration pack, Void arg) {
        currentPackage = pack.getNameAsString();

        for (VariableDeclarator f : fields){
            methods.forEach((method1, body1) -> {
                methods.forEach((method2, body2) -> {
                    if(!method1.equals(method2)){    
                        if(body1.toString().contains(f.toString()) && body2.toString().contains(f.toString() )) {
                            if(!methodPairs.containsKey(method1)  && !methodPairs.containsKey(method2)){
                                methodPairs.put(method1, method2);
                            }
                        }
                    }
                });
            }); 
        }

    
        nb(classesWithNodes);
    }



    /**
     *
     * @param n number of methode connected
     * @return  Tight Class Cohésion
     */
    public int TCC(int n ){
        int NP = NP(n);
        int NDC = NDC();
//        System.out.println(NDC/NP);
        return NDC/NP;
    }

    /**
     *
     * @return  the number of pairs of directly related methods
     */
    public int NDC(){

        return methodPairs.size();
    }

    /**
     * @param n number of methode connected
     * @return the maximum possible number possible pairs of connected methods
     */
    public int NP(int n) {
        return (n* (n-1))/2 ;
    }

    /***
     * function allows to calculate number of arc between all methode used same attributs. 
     * nb arc 
     */
    public void nb(Map<String, Integer> classesWithNodes){
        Map<String, Integer> NPEachClass= new HashMap<>();
        for (Map.Entry mapentry : classesWithNodes.entrySet()) {
            int nbArcPossibles =  NP((int) mapentry.getValue());
            NPEachClass.put(mapentry.getKey().toString(), nbArcPossibles);
        }

        for (Map.Entry c2 : NPEachClass.entrySet()){
            packageClassField.put(currentPackage, new Pair<String,Integer>(currentCLass, ((Integer)c2.getValue())/this.methodPairs.size()));
        }

    }

    public int getMethodsSize(){
        return this.methods.size();
    }


}

```

Exportation en CSV: 

```Java

package fr.istic.vv;

import java.io.File;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;

public class Main {

    private File csvOutputFile = new File("Exerice5_JavaParser.csv");
    private List<String[]> dataLines = new ArrayList<>();
    private String pathDirectory= "/home/ougueur/Bureau/M2_ILAAAA/vv/commons-collections/src/main/java/org/apache/commons/collections4";
    //private String pathDirectory="/home/ougueur/Bureau/M2_ILAAAA/vv/commons-collections/src/test/java/org/apache/commons/collections4";
    /**
     * FUnction allows to calculate  the maximum number of possible connections
     */

    public static int NumberofPossibleConnections(int nbNodes){
        return (nbNodes * (nbNodes - 1)) / 2;
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
    public void exportCSV(Map<String, Pair<String, Integer>> arg) throws IOException {

        PrintWriter pw = null;

        for ( String p : arg.keySet()) {
            
            dataLines.add(new String[] { "Package : " + p , " Class : "+ arg.get(p).a.toString(),  "Fields Without Getter : " + arg.get(p).b.toString() });
            
        }

        try {
            pw = new PrintWriter(csvOutputFile);
            dataLines.stream().map(this::convertToCSV).forEach(pw::println);

        } catch( Exception e){
            e.printStackTrace();
        } finally {
            pw.close();
        }
    }

    public static void main(String [] args) throws Exception{

        Main csv = new Main();

        File file = new File(csv.pathDirectory);
        if(!file.exists() || !file.isDirectory() || !file.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }

        SourceRoot root = new SourceRoot(file.toPath());
        PublicElementsPrinter printer = new PublicElementsPrinter();


        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> unit.accept(printer, null));
            return SourceRoot.Callback.Result.DONT_SAVE;
        });

        ; 

        printer.getPackageClassField();

        try {
            csv.exportCSV(printer.getPackageClassField());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 

       Map<String, Integer> classesWithNodes = printer.getClassesWithNodes();
        for (Map.Entry entry : classesWithNodes.entrySet()){
           int nbNodes =(int) entry.getValue();
            int NbConnections = NumberofPossibleConnections(nbNodes);
            System.out.println(" nb connection pour " + NbConnections + " la class " + entry.getKey());

        } 
    }


}

```