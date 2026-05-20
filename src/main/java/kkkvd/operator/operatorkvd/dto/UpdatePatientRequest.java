package kkkvd.operator.operatorkvd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePatientRequest {
    //Редактировать поля пациента (личные данные)

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;
    @NotBlank(message = "Имя обязательно")
    private String firstName;
    private String middleName;
    @NotNull(message = "Дата рождения обязательна")
    private LocalDate birthDate;
    @NotNull(message = "Пол обязателен")
    private Long genderId;
    private String address;
}
