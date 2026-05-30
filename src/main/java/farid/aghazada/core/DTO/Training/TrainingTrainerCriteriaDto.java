package farid.aghazada.core.DTO.Training;

import java.time.LocalDate;

import farid.aghazada.core.Entity.TrainingType;

public record TrainingTrainerCriteriaDto(
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName,
        TrainingType trainingType
) {
}
