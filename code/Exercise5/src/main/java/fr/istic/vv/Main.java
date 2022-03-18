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
