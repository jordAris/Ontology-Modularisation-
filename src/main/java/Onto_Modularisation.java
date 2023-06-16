import repositories.modularisation_service;

public class Onto_Modularisation {
    public static void main(String[] args) {
        String ontologyFilePath = "path_to_your_ontology.owl";

        modularisation_service modularizationSystem = new modularisation_service(ontologyFilePath);
        modularizationSystem.Modularize();
    }
}
