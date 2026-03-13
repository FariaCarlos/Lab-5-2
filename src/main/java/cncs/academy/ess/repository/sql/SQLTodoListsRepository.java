package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLTodoListsRepository implements TodoListsRepository {

    private final BasicDataSource dataSource;

    public SQLTodoListsRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TodoList findById(int listId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt =
                     connection.prepareStatement("SELECT * FROM lists WHERE id = ?")) {

            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTodoList(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todo list by id", e);
        }
        return null;
    }

    @Override
    public List<TodoList> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt =
                     connection.prepareStatement("SELECT * FROM lists");
             ResultSet rs = stmt.executeQuery()) {

            List<TodoList> lists = new ArrayList<>();
            while (rs.next()) {
                lists.add(mapResultSetToTodoList(rs));
            }
            return lists;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all todo lists", e);
        }
    }

    @Override
    public List<TodoList> findAllByUserId(int userId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt =
                     connection.prepareStatement("SELECT * FROM lists WHERE owner_id = ?")) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<TodoList> lists = new ArrayList<>();
                while (rs.next()) {
                    lists.add(mapResultSetToTodoList(rs));
                }
                return lists;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todo lists by user id", e);
        }
    }

    @Override
    public int save(TodoList todoList) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt =
                     connection.prepareStatement(
                             "INSERT INTO lists (name, owner_id) VALUES (?, ?)",
                             Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, todoList.getName());
            stmt.setInt(2, todoList.getOwnerId());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save todo list", e);
        }
        return 0;
    }

    @Override
    public void update(TodoList todoList) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt =
                     connection.prepareStatement("UPDATE lists SET name = ? WHERE id = ?")) {

            stmt.setString(1, todoList.getName());
            stmt.setInt(2, todoList.getListId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update todo list", e);
        }
    }

    @Override
    public boolean deleteById(int listId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt =
                     connection.prepareStatement("DELETE FROM lists WHERE id = ?")) {

            stmt.setInt(1, listId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete todo list", e);
        }
    }

    /* ===== Método auxiliar (igual ao padrão do SQLUserRepository) ===== */

    private TodoList mapResultSetToTodoList(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int userId = rs.getInt("owner_id");
        return new TodoList(id, name, userId);
    }
}
