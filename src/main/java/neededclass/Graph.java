package neededclass;

import java.util.Collection;

public class Graph {
    private Collection<String> nodes;
    private Collection<Edge> edges;

    public Graph(Collection<String> nodes, Collection<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Collection<String> getNodes() {
        return nodes;
    }

    public int getDegree(String node) {
        int degree = 0;
        for (Edge edge : edges) {
            if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
                degree++;
            }
        }
        return degree;
    }
}
