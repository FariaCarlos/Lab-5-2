import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void saveAndFindById_ShouldReturnSavedUser() {
        User user = new User("jane", "hash1", "salt1", "BASE");

        int id = repository.save(user);
        User savedUser = repository.findById(id);

        assertNotNull(savedUser);
        assertEquals("jane", savedUser.getUsername());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        repository.save(new User("u1", "h1", "s1", "BASE"));
        repository.save(new User("u2", "h2", "s2", "ADMIN"));

        List<User> users = repository.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User user = new User("ana", "h1", "s1", "BASE");
        int id = repository.save(user);

        repository.deleteById(id);

        assertNull(repository.findById(id));
    }

    @Test
    void findByUsername_ShouldReturnCorrectUser() {
        repository.save(new User("john", "h1", "s1", "BASE"));

        User found = repository.findByUsername("john");

        assertNotNull(found);
        assertEquals("john", found.getUsername());
    }

    @Test
    void findByUsername_ShouldReturnNullIfNotExists() {
        User found = repository.findByUsername("unknown");

        assertNull(found);
    }

    @Test
    void save_ShouldRespectExistingId() {
        User user = new User(10, "maria", "h1", "s1", "BASE");

        int returnedId = repository.save(user);

        assertEquals(10, returnedId);
        assertNotNull(repository.findById(10));
    }
}