package kkkvd.operator.operatorkvd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefDto {

    //Справочники (показать имя, иметь id для селекта)
    private Long id;
    private String name;
}
