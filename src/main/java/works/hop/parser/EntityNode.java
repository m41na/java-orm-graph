package works.hop.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityNode {

    String name;
    String table;
    String namespace;
    Map<String, FieldNode> fields;
}
