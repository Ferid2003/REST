package farid.aghazada.core.DTO.Training;

import java.time.LocalDate;

import farid.aghazada.core.Entity.TrainingType;

public record TrainingTraineeCriteriaDto(
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingType trainingType
) {
}
