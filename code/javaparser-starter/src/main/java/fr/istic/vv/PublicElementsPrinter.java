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

    File csvOutputFile = new File("Exerice3_JavaParser.csv");
    
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
       
         PrintWriter pw = null;
        
        for ( String p : arg.keySet()){
            if(!arg.get(p).b.isEmpty()){
                dataLines.add(new String[] { "Package : " + p , " Class : "+ arg.get(p).a.toString(),  "Fields Without Getter : " + arg.get(p).b.toString() });
            }
                
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
 
}
