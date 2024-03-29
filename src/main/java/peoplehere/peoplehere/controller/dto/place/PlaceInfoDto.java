package peoplehere.peoplehere.controller.dto.place;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfoDto {
    private Long placeId;
    private String placeName;
    private List<String> placeImages;
    private String placeAddress;
    private Point latLng;
    private int placeOrder;
}
