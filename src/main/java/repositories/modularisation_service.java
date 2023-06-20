package repositories;

import edu.stanford.smi.protege.model.*;
import neededclass.CosineSimilarity;
import neededclass.Edge;
import neededclass.Graph;
import neededclass.SimilarityMeasure;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.tartarus.snowball.ext.PorterStemmer;

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
        Map<String, Double> semanticDistance = calculateSemanticDistance(ontologyGraph);

        evaluateCriteria(nodeCentrality, semanticDistance);

        // define now the real criteria that we'll use for the ontology modularization

        List<String> moduleCriteria = new ArrayList<>();
        Map<String, String> projectRequirements = getProjectRequirements();

        for (String node : nodeCentrality.keySet()) {
            int centrality = nodeCentrality.get(node);
            double semanticDistances = semanticDistance.get(node);

            String criteria = "";
            if (centrality >= Integer.parseInt(projectRequirements.get("centrality"))){
                criteria += "centrality";
            }
            if (semanticDistances <= Double.parseDouble(projectRequirements.get("semanticDistance"))) {
                criteria += "semanticDistance";
            }

            if (criteria.length() > 0) {
                moduleCriteria.add(criteria);
            }
        }

        //algorithm that it create modules from our ontology represented as graph according to the different module criteria

        List<List<String>> modules = new ArrayList<>();

        for (String node : nodes){
            if(modules.isEmpty()){
                List<String> module = new ArrayList<>();
                module.add(node);
                modules.add(module);
            } else {
                boolean found = false;
                for (List<String> module : modules) {
                    if (moduleCriteria.contains("centrality") && nodeCentrality.get(node) >= Integer.parseInt(moduleCriteria.get(moduleCriteria.indexOf("centrality")))) {
                        if (moduleCriteria.contains("semanticDistance") && semanticDistance.get(node + "," + module.get(0)) <= Double.parseDouble(moduleCriteria.get((moduleCriteria.indexOf("semanticDistance"))))){
                            module.add(node);
                            found = true;
                            break;
                        }

                    } else if (!moduleCriteria.contains("centrality") && semanticDistance.get(node + "," + module.get(0)) <= Double.parseDouble(moduleCriteria.get(moduleCriteria.indexOf("semanticDistance")))) {
                        module.add(node);
                        found = true;
                        break;
                    }
                }

                if (!found){
                    List<String> module = new ArrayList<>();
                    module.add(node);
                    modules.add(module);
                }
            }
        }

        // algorithm to validate the different module created (hint: the first document sent by the teacher)

        for (List<String> module : modules) {
            int numInternalEdges = 0;
            int numPossibleInternalEdges = (module.size() * (module.size() - 1)) /2 ;

            for (int i= 0; i < module.size() - 1; i++){
                String node1 = module.get(i);
                for (int j = i+1; j< module.size(); j++) {
                    String node2 = module.get(j);
                    if(ontologyGraph.containsEdge(node1, node2)) {
                        numInternalEdges ++;
                    }
                }
            }

            double cohesion = (double) numInternalEdges/numPossibleInternalEdges;
            int numExternalEdges = 0;
            int numPossibleExternalEdges = module.size() * (ontologyGraph.getNodeCount() - module.size());

            for (String node : module) {
                for (List<String> otherModule : modules ) {
                    if (otherModule != module) {
                        for (String otherNode : otherModule) {
                            if (ontologyGraph.containsEdge(node, otherNode))
                                numExternalEdges++;
                        }
                    }
                }
            }

            double coupling = (double) numExternalEdges /numPossibleExternalEdges;
            double modularizationQuality = (cohesion-coupling)/(cohesion+coupling);

            System.out.println("Module: " + module);
            System.out.println("Cohesion: " + cohesion);
            System.out.println("Coupling: " + coupling);
            System.out.println("Modularization Quality: " + modularizationQuality);
            System.out.println();
        }


        // a way to make our modules visble

        System.out.println("The modules are:");
        for (List<String> module : modules) {
            System.out.println("Module: " + module);
            System.out.println(" Nodes: " + module.toString());
            System.out.println();
        }

    }

    private Map<String, Double> calculateSemanticDistance(Graph graph) {
        Map<String, Double> semanticDistance = new HashMap<>();

        for (Object cls : project.getKnowledgeBase().getClses()){
            if (cls instanceof Cls) {
                String className = ((Cls) cls).getName();
                double maxSimilarity = 0.0;

                for (Object otherCls: project.getKnowledgeBase().getClses()) {
                    if (otherCls instanceof Cls) {
                        String otherClasname = ((Cls) otherCls).getName();
                        double similarity = calculateSemanticSimilarity(className, otherClasname);

                        if (similarity > maxSimilarity) {
                            maxSimilarity = similarity;
                        }
                    }
                }

                semanticDistance.put(className, 1- maxSimilarity);
            }
        }

        return semanticDistance;
    }

    private double calculateSemanticSimilarity(String className, String otherClasname) {
        // Define a similarity measure.
        SimilarityMeasure similarityMeasure = new CosineSimilarity();

        // Determine the concept properties.
        List<String> classNameProperties = Arrays.asList("label", "description", "synonyms");
        List<String> otherClassNameProperties = Arrays.asList("label", "description", "synonyms");

        // Preprocess the concept properties.
        List<String> processedClassNameProperties = preprocess(classNameProperties);
        List<String> processedOtherClassNameProperties = preprocess(otherClassNameProperties);

        // Calculate similarity scores.
        List<Double> similarityScores = new ArrayList<>();
        for (String property : classNameProperties) {
            double similarityScore = similarityMeasure.calculateSimilarity(processedClassNameProperties.get(0), processedOtherClassNameProperties.get(0));
            similarityScores.add(similarityScore);
        }

        // Aggregate similarity scores.
        double similarityScore = 0;
        for (double score : similarityScores) {
            similarityScore += score;
        }
        similarityScore /= similarityScores.size();

        // Normalize similarity scores.
        similarityScore = Math.min(Math.max(similarityScore, 0.0), 1.0);

        return similarityScore;
    }

    private List<String> preprocess(List<String> properties) {
        List<String> processedProperties = new ArrayList<>();
        for (String property : properties) {
            property = property.toLowerCase();
            property = removeStopWords(property);
            property = stem(property);
            processedProperties.add(property);
        }
        return processedProperties;
    }

    private String removeStopWords(String property) {
        Set<String> stopWords = new HashSet<>(Arrays.asList("the", "of", "and", "to", "in", "a", "that", "is", "was"));
        return property.replaceAll(" " + stopWords.toString() + " ", " ");
    }

    private String stem(String property) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(property);
        stemmer.stem();
        return stemmer.getCurrent();
    }


    private void printModules(List<List<String>> modules) {
    }


    private Map<String, String> getProjectRequirements() {
        Map<String, String> projectRequirements = new HashMap<>();

        projectRequirements.put("centrality", "10");
        projectRequirements.put("semanticDistance", "5");

        return projectRequirements;
    }



    private Map<String, Integer> calculateNodeCentrality(Graph graph) {
        Map<String, Integer> nodeCentrality = new HashMap<>();

        for (String node : graph.getNodes()) {
            int degree = graph.getDegree(node);
            nodeCentrality.put(node, degree);
        }

        return nodeCentrality;
    }

    private void evaluateCriteria(Map<String, Integer> nodeCentrality, Map<String, Double> semanticDistance) {
        Map<String, Integer> nodeRanks = new HashMap<>();

        for (String node : nodeCentrality.keySet()){
            int centrality = nodeCentrality.get(node);
            double semanticDistances = semanticDistance.get(node);

            int rank = centrality + (int) (semanticDistances * 10);
        }

        List<String> rankedNodes = new ArrayList<>(nodeRanks.keySet());
        Collections.sort(rankedNodes, (n1, n2) -> nodeRanks.get(n1)-nodeRanks.get(n2));

        for (int i= 0; i< 10; i++) {
            System.out.println(rankedNodes.get(i));
        }
    }


}
