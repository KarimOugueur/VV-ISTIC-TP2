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

    public Map<String, Pair<String, Integer>> getPackageClassField(){
        return this.packageClassField; 
    }

    


}

