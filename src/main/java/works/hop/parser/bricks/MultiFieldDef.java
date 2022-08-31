package works.hop.parser.bricks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiFieldDef {

    List<String> columnNames;
    PropertyFieldType propertyFieldType;
}
