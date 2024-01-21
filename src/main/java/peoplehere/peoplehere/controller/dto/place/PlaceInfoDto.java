package peoplehere.peoplehere.controller.dto.place;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceInfoDto {
    private Long id;
    private String content;
    private String imageUrl;
    private String address;
    private int order;
}
