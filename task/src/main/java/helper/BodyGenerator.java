package helper;

import lombok.experimental.UtilityClass;
import service.dto.request.EntityRequest;

@UtilityClass
public class BodyGenerator {

    public static EntityRequest.EntityRequestBuilder getPostEntity() {
        return EntityRequest.builder();
    }
}
