package farid.aghazada.core.DTO;

public record RegistrationResponseDto(
        String username,
        String password
) {
    public static RegistrationResponseDto toRegistrationResponseDto(String username, String password) {
        return new RegistrationResponseDto(username, password);
    }

}

