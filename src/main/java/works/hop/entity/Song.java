// Autogenerated class. Do not modify
package works.hop.entity;

import java.lang.Integer;
import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import works.hop.generate.Entity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Song implements Entity {
  private MusicAlbum album;

  private String description;

  private Integer id;

  private String title;
}
