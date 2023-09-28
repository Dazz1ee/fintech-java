package foo.fintech.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
public class Weather {
    private final Long regionId;

    private final String regionName;

    private Double temperature;

    private LocalDateTime date;


//    public void setRegionId(Long regionId) {
//        if (this.regionId == null) {
//            this.regionId = regionId;
//        }
//    }
}
