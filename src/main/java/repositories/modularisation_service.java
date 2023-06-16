package repositories;

import edu.stanford.smi.protege.model.*;
import neededclass.Edge;
import neededclass.Graph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public class modularisation_service {
    private Project project;

    // constructor that enter in the ontology file provided in entry
    public modularisation_service(String OntologyEntryPath) {
        try {
            Collection<String> errors = new ArrayList<>();
            project = new Project(OntologyEntryPath, errors);

            if (!errors.isEmpty()){
                System.err.println("Errors occurred while loading the ontology:");
                for (String error : errors) {
                    System.err.println(error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Modularize() {

        // extract the different elements of ontology as I can analyze easier

        Collection<String> nodes = new HashSet<>();
        Collection<Edge> edges = new HashSet<>();

        for (Object cls: project.getKnowledgeBase().getClses()) {

            if ( cls instanceof Cls){
                String className = ((Cls)cls).getName();
                nodes.add(className);

                for (Slot slot: ((Cls)cls).getOwnSlots()) {
                    String slotName = slot.getName();

                    for (Object val: ((Cls)cls).getDirectOwnSlotValues(slot)){
                        if (val instanceof Cls) {
                            String relatedClassName = ((Cls) val).getName();
                        }
                    }
                }
            }

        }

        // represent now the ontology as a graph

        Graph ontologyGraph = new Graph(nodes, edges);

        // analyze the graph

        Map<String, Integer> nodeCentrality = calculateNodeCentrality(ontologyGraph);

        for (String node: nodeCentrality.keySet()) {
            int centrality = nodeCentrality.get(node);
            System.out.println("Node: " + node + ", Degree Centrality: " + centrality);
        }

        // define the first criteria for the ontology

        for (Object cls: project.getKnowledgeBase().getClses()) {
            if (cls instanceof Cls) {
                String className = ((Cls) cls).getName();
                // Example criteria evaluation
                boolean meetsCriteria = evaluateCriteria(className);
                if (meetsCriteria) {
                    System.out.println(className + " meets the criteria.");
                } else {
                    System.out.println(className + " does not meet the criteria.");
                }
            }
        }

        // define now the real criteria that we'll use for the ontology modularization

        List<String> moduleCriteria = defineModuleCriteria();


        //algorithm that it create modules from our ontology represented as graph according to the different module criteria

        List<List<String>> modules = createModules(moduleCriteria);

        // algorithm to validate the different module created (hint: the first document sent by the teacher)

        validateModules(modules);

        // a way to make our modules visble

        printModules(modules);

    }

    private List<List<String>> createModules(List<String> moduleCriteria) {

        return null;
    }

    private void printModules(List<List<String>> modules) {
    }

    private void validateModules(List<List<String>> modules) {
    }

    private List<String> defineModuleCriteria() {
        List<String> modCriteria = new ArrayList<>();

        try {
            Properties properties = new Properties();
            FileInputStream input = new FileInputStream("criteria.properties");
            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                String criterion = properties.getProperty(key);
                modCriteria.add(criterion);
            }

            input.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return modCriteria;
    }

    private Map<String, Integer> calculateNodeCentrality(Graph graph) {
        Map<String, Integer> nodeCentrality = new HashMap<>();

        for (String node : graph.getNodes()) {
            int degree = graph.getDegree(node);
            nodeCentrality.put(node, degree);
        }

        return nodeCentrality;
    }

    private boolean evaluateCriteria(String className) {

        Cls cls = project.getKnowledgeBase().getCls(className);
        return className.startsWith("prefix");
    }


}
