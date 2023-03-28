package service.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityResponse {

    int id;
    String text;
    boolean completed;
}