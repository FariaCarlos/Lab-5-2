package cncs.academy.ess.service;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;

import java.util.Collection;

public class TodoListsService {
    TodoListsRepository todoListsRepository;

    public TodoListsService(TodoListsRepository todoListsRepository) {
        this.todoListsRepository = todoListsRepository;
    }

    public TodoList createTodoListItem(String listName, int ownerId) {
        TodoList list = new TodoList(listName, ownerId);
        int listId = todoListsRepository.save(list);
        list.setId(listId);
        return list;
    }
    public TodoList getTodoList(int listId) {
        return todoListsRepository.findById(listId);
    }
    public Collection<TodoList> getAllTodoLists(int userId) {
        return todoListsRepository.findAllByUserId(userId);
    }
}
