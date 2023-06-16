package repositories;

import edu.stanford.smi.protege.model.*;
import neededclass.Edge;
import neededclass.Graph;

import java.util.*;


public class modularisation_service {
    private Project project;
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

        Graph ontologyGraph = new Graph(nodes, edges);

        Map<String, Integer> nodeCentrality = calculateNodeCentrality(ontologyGraph);

        for (String node: nodeCentrality.keySet()) {
            int centrality = nodeCentrality.get(node);
            System.out.println("Node: " + node + ", Degree Centrality: " + centrality);
        }

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

        List<String> moduleCriteria = defineModuleCriteria();


        List<List<String>> modules = createModules(moduleCriteria);

        validateModules(modules);

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


        return null;
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
