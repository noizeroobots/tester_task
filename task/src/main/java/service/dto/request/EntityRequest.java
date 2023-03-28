package service.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(buildMethodName = "please", setterPrefix = "with")
public class EntityRequest {

    @Builder.Default
    private final int id = 0;

    @Builder.Default
    private final String text = "test";

    @Builder.Default
    private final boolean completed = false;
}