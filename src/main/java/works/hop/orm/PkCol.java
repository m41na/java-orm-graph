package works.hop.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PkCol {

    String name;
    String pkName;
    int keySeq;
}
