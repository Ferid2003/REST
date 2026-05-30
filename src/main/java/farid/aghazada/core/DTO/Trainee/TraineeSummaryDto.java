package farid.aghazada.core.DTO.Trainee;

import farid.aghazada.core.Entity.Trainee;

public record TraineeSummaryDto(
        String username,
        String firstName,
        String lastName
){
    public static TraineeSummaryDto toTraineeSummaryDto(Trainee trainee) {
        return new TraineeSummaryDto(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName()
        );
    }
}
