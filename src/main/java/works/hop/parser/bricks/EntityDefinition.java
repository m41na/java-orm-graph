package works.hop.parser.bricks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDefinition {

    String tableName;
    String entityName;
    FieldDefinitions fieldDefinitions;
}
