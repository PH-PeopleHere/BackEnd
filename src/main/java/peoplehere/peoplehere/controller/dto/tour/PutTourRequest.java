package peoplehere.peoplehere.controller.dto.tour;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import peoplehere.peoplehere.controller.dto.place.PlaceInfoDto;
import peoplehere.peoplehere.controller.dto.place.PutPlaceRequest;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PutTourRequest {
    @NotBlank(message = "투어 이름은 필수입니다.")
    @Size(max = 100, message = "투어 이름은 최대 100자까지 가능합니다.")
    private String tourName;

    @Min(value = 0, message = "시간은 0 이상이어야 합니다.")
    private int tourTime;

    private String tourContent;

    private List<String> categoryNames;

    private List<PutPlaceRequest> places;

    private List<Long> deletedPlaceIds;

}
