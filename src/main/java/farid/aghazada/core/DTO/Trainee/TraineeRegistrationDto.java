package farid.aghazada.core.DTO.Trainee;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

import farid.aghazada.core.Entity.Trainee;
import farid.aghazada.core.Entity.User;

public record TraineeRegistrationDto(
        @NotBlank(message = "First name cannot be blank")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        String lastName,
        LocalDate dateOfBirth,
        String address
        ){

    public static Trainee toTrainee(TraineeRegistrationDto dto, String username, String password){
        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setDateOfBirth(dto.dateOfBirth());
        trainee.setAddress(dto.address());
        trainee.setUser(user);

        return trainee;
    }

}
