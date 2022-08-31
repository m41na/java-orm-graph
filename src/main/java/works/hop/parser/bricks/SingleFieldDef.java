package works.hop.parser.bricks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleFieldDef {

    String columnName;
    PropertyFieldType propertyFieldType;
}
