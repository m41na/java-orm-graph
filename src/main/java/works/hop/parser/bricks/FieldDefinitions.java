package works.hop.parser.bricks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinitions {

    List<SingleFieldDef> singleFieldDefinitions = new LinkedList<>();
    List<MultiFieldDef> multiFieldDefinitions = new LinkedList<>();
}
