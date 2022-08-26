package works.hop.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class Graph<I extends Comparable<I>, T extends Comparable<T>> {

    List<I> nodes = new ArrayList<>();
    Map<I, List<Node<I, T>>> adjacency = new HashMap<>();

    public static Graph<Character, Character> example1() {
        Graph<Character, Character> list = new Graph<>();
        list.nodes.add('C');
        list.adjacency.put('C', List.of(Node.of('B', 'B'), Node.of('A', 'A')));
        list.nodes.add('B');
        list.adjacency.put('B', List.of(Node.of('D', 'D')));
        list.nodes.add('A');
        list.adjacency.put('A', List.of(Node.of('D', 'D')));
        list.nodes.add('D');
        list.adjacency.put('D', List.of(Node.of('G', 'G'), Node.of('H', 'H')));
        list.nodes.add('E');
        list.adjacency.put('E', List.of(Node.of('A', 'A'), Node.of('D', 'D'), Node.of('F', 'F')));
        list.nodes.add('F');
        list.adjacency.put('F', List.of(Node.of('K', 'K'), Node.of('J', 'J')));
        list.nodes.add('G');
        list.adjacency.put('G', List.of(Node.of('I', 'I')));
        list.nodes.add('H');
        list.adjacency.put('H', List.of(Node.of('J', 'J'), Node.of('I', 'I')));
        list.nodes.add('I');
        list.adjacency.put('I', List.of(Node.of('L', 'L')));
        list.nodes.add('J');
        list.adjacency.put('J', List.of(Node.of('L', 'L'), Node.of('M', 'M')));
        list.nodes.add('K');
        list.adjacency.put('K', List.of(Node.of('J', 'J')));
        list.nodes.add('L');
        list.adjacency.put('L', emptyList());
        list.nodes.add('M');
        list.adjacency.put('M', emptyList());
        return list;
    }

    public static Graph<Integer, Integer> example2() {
        Graph<Integer, Integer> list = new Graph<>();
        list.nodes.add(2);
        list.adjacency.put(2, List.of(Node.of(0, 0), Node.of(4, 4)));
        list.nodes.add(0);
        list.adjacency.put(0, List.of(Node.of(1, 1), Node.of(3, 3)));
        list.nodes.add(4);
        list.adjacency.put(4, List.of(Node.of(3, 3), Node.of(5, 5)));
        list.nodes.add(3);
        list.adjacency.put(3, List.of(Node.of(1, 1)));
        list.nodes.add(5);
        list.adjacency.put(5, List.of(Node.of(1, 1)));
        return list;
    }

    public static <I extends Comparable<I>, T extends Comparable<T>> List<I> topSort(Graph<I, T> graph) {
        List<I> ordered = new ArrayList<>();
        for (I node : graph.nodes) {
            if (!ordered.contains(node)) {
                graph.dfs(ordered, node);
            }
        }
        return ordered;
    }

    public static void main(String[] args) {
        Graph<Character, Character> charGraph = example1();
        List<Character> sortedChars = topSort(charGraph);
        System.out.printf("%s%n", sortedChars);
        Graph<Integer, Integer> intGraph = example2();
        List<Integer> sortedInts = topSort(intGraph);
        System.out.printf("%s%n", sortedInts);
    }

    public void add(I nodeId){
        if(!nodes.contains(nodeId)){
            nodes.add(nodeId);
            adjacency.putIfAbsent(nodeId, new ArrayList<>());
        }
    }

    public void addChild(I nodeId, T nodeValue){
        add(nodeId);
        if(adjacency.get(nodeId).stream().noneMatch(n -> n.identifier.equals(nodeId))) {
            adjacency.get(nodeId).add(Node.of(nodeId, nodeValue));
        }
    }

    public void dfs(List<I> visited, I nodeId) {
        if (adjacency.containsKey(nodeId)) {
            for (Node<I, T> child : adjacency.get(nodeId)) {
                if (!visited.contains(child.identifier)) {
                    dfs(visited, child.identifier);
                }
            }
        }
        visited.add(0, nodeId);
    }
}
