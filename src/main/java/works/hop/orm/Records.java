package works.hop.orm;

import works.hop.generate.Entity;

import java.util.HashMap;
import java.util.Map;

public class Records<T extends Comparable<T>, E extends Entity> extends HashMap<T, Map<T, E>> {

    public void cache(T table, T pk, E entity) {
        putIfAbsent(table, new HashMap<>());
        Map<T, E> records = get(table);
        records.putIfAbsent(pk, entity);
    }

    public E retrieve(T table, T pk) {
        if (containsKey(table)) {
            Map<T, E> records = get(table);
            if (records.containsKey(pk)) {
                return records.get(pk);
            }
        }
        return null;
    }
}
