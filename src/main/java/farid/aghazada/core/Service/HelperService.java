package farid.aghazada.core.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import farid.aghazada.core.Entity.User;
import farid.aghazada.core.Exception.AuthenticationException;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.UserRepository;

@Service
public class HelperService {


    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUsername(String firstName, String lastName){
        String username = firstName + "." + lastName;
        int suffix = getUsernameSuffix(username);
        if(suffix > 0) {
            username += suffix;
        }
        return username;
    }

    public String generatePassword(){
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private int getUsernameSuffix(String baseUsername){
        List<String> matchingUsernames = userRepository.findAllUsernamesForBase(baseUsername);
        if (matchingUsernames.isEmpty()) {
            return 0;
        }

        int maxSuffix = 0;
        boolean baseTaken = false;

        for (String username : matchingUsernames) {
            if (username.equals(baseUsername)) {
                baseTaken = true;
                continue;
            }
            if (!username.startsWith(baseUsername)) {
                continue;
            }

            String suffix = username.substring(baseUsername.length());
            if (suffix.chars().allMatch(Character::isDigit)) {
                int suffixValue = Integer.parseInt(suffix);
                if (suffixValue > maxSuffix) {
                    maxSuffix = suffixValue;
                }
            }
        }

        if (!baseTaken) {
            return 0;
        }

        return maxSuffix + 1;
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Username and password do not match");
        }

        return true;
    }


}
