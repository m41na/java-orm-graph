package works.hop.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldNode implements Visitable{

    String name;
    String column;
    String type;

    @Override
    public void accept(Visitor visitor) {
        visitor.visitField(this);
    }
}
