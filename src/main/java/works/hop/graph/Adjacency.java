package works.hop.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class Adjacency<T extends Comparable<T>> {

    List<T> nodes = new ArrayList<>();
    Map<T, List<T>> graph = new HashMap<>();

    public static Adjacency<Character> example1() {
        Adjacency<Character> list = new Adjacency<>();
        list.nodes.add('C');
        list.graph.put('C', List.of('B', 'A'));
        list.nodes.add('B');
        list.graph.put('B', List.of('D'));
        list.nodes.add('A');
        list.graph.put('A', List.of('D'));
        list.nodes.add('D');
        list.graph.put('D', List.of('G', 'H'));
        list.nodes.add('E');
        list.graph.put('E', List.of('A', 'D', 'F'));
        list.nodes.add('F');
        list.graph.put('F', List.of('K', 'J'));
        list.nodes.add('G');
        list.graph.put('G', List.of('I'));
        list.nodes.add('H');
        list.graph.put('H', List.of('J', 'I'));
        list.nodes.add('I');
        list.graph.put('I', List.of('L'));
        list.nodes.add('J');
        list.graph.put('J', List.of('L', 'M'));
        list.nodes.add('K');
        list.graph.put('K', List.of('J'));
        list.nodes.add('L');
        list.graph.put('L', emptyList());
        list.nodes.add('M');
        list.graph.put('M', emptyList());
        return list;
    }

    public static Adjacency<Integer> example2() {
        Adjacency<Integer> list = new Adjacency<>();
        list.nodes.add(2);
        list.graph.put(2, List.of(0, 4));
        list.nodes.add(0);
        list.graph.put(0, List.of(1, 3));
        list.nodes.add(4);
        list.graph.put(4, List.of(3, 5));
        list.nodes.add(3);
        list.graph.put(3, List.of(1));
        list.nodes.add(5);
        list.graph.put(5, List.of(1));
        return list;
    }

    public static <T extends Comparable<T>> List<T> topSort(Adjacency<T> graph) {
        List<T> ordered = new ArrayList<>();
        for (T node : graph.nodes) {
            if (!ordered.contains(node)) {
                graph.dfs(ordered, node);
            }
        }
        return ordered;
    }

    public static void main(String[] args) {
        Adjacency<Character> charGraph = example1();
        List<Character> sortedChars = topSort(charGraph);
        System.out.printf("%s%n", sortedChars);
        Adjacency<Integer> intGraph = example2();
        List<Integer> sortedInts = topSort(intGraph);
        System.out.printf("%s%n", sortedInts);
    }

    public void add(T node){
        if(!nodes.contains(node)){
            nodes.add(node);
            graph.putIfAbsent(node, new ArrayList<>());
        }
    }

    public void addChild(T parent, T child){
        add(parent);
        if(!graph.get(parent).contains(child)) {
            graph.get(parent).add(child);
        }
    }

    public void dfs(List<T> visited, T node) {
        if (graph.containsKey(node)) {
            for (T child : graph.get(node)) {
                if (!visited.contains(child)) {
                    dfs(visited, child);
                }
            }
        }
        visited.add(0, node);
    }
}
