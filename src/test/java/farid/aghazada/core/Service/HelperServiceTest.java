package farid.aghazada.core.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import farid.aghazada.core.Entity.User;
import farid.aghazada.core.Exception.AuthenticationException;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class HelperServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HelperService helperService;

    @Test
    void generateUsernameReturnsBaseWhenNotTaken() {
        when(userRepository.findAllUsernamesForBase("Jane.Doe")).thenReturn(List.of());

        String result = helperService.generateUsername("Jane", "Doe");

        assertThat(result).isEqualTo("Jane.Doe");
    }

    @Test
    void generateUsernameReturnsNextSuffixWhenTaken() {
        when(userRepository.findAllUsernamesForBase("Jane.Doe"))
                .thenReturn(List.of("Jane.Doe", "Jane.Doe1", "Jane.Doe3"));

        String result = helperService.generateUsername("Jane", "Doe");

        assertThat(result).isEqualTo("Jane.Doe4");
    }

    @Test
    void generatePasswordReturnsTenCharacterAlphaNumericString() {
        String password = helperService.generatePassword();

        assertThat(password).hasSize(10);
        assertThat(password).matches("^[A-Za-z0-9]{10}$");
    }

    @Test
    void authenticateReturnsTrueWhenPasswordMatches() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("secret");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        boolean result = helperService.authenticate("john", "secret");

        assertThat(result).isTrue();
    }

    @Test
    void authenticateThrowsWhenUserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> helperService.authenticate("john", "secret"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("john");
    }

    @Test
    void authenticateThrowsWhenPasswordMismatched() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("secret");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> helperService.authenticate("john", "bad"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("do not match");
    }
}
