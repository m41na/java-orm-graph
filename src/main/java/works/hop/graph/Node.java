package works.hop.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node <I extends Comparable<I>, T extends Comparable<T>>{

    I identifier;
    T node;

    public static <I extends Comparable<I>, T extends Comparable<T>> Node<I, T> of(I identifier, T value){
        return new Node<I, T>(identifier, value);
    }
}
