import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.cs3354Team3.cs3354GroupProject.entity.Role;
import com.cs3354Team3.cs3354GroupProject.entity.User;
import com.cs3354Team3.cs3354GroupProject.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class Cs3354GroupProjectApplication {

	public static void main(String[] args) {
        SpringApplication.run(Cs3354GroupProjectApplication.class, args);
	}
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo, BCryptPasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() == 0) {
                userRepo.save(new User(null, "admin@uni.com", encoder.encode("admin123"), Role.ADMIN));
                userRepo.save(new User(null, "teacher@uni.com", encoder.encode("teach123"), Role.TEACHER));
                userRepo.save(new User(null, "student@uni.com", encoder.encode("stud123"), Role.STUDENT));
            }
        };
    }

}
