package cncs.academy.ess;

import cncs.academy.ess.controller.AuthorizationMiddleware;
import cncs.academy.ess.controller.TodoController;
import cncs.academy.ess.controller.TodoListController;
import cncs.academy.ess.controller.UserController;
import cncs.academy.ess.repository.sql.SQLUserRepository;
import cncs.academy.ess.service.TodoListsService;
import cncs.academy.ess.service.TodoUserService;
import cncs.academy.ess.service.TodoService;
import io.javalin.Javalin;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;

import cncs.academy.ess.repository.sql.SQLTodoListsRepository;
import cncs.academy.ess.repository.sql.SQLTodoRepository;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.jcasbin.persist.file_adapter.FileAdapter;

public class App {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });

            // Servir ficheiros estáticos
            config.staticFiles.add("/public");

        }).start(7100);

        // Initialize routes for user management
        //InMemoryUserRepository userRepository = new InMemoryUserRepository();
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        String connectURI = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", "localhost", "5432", "postgres", "postgres", "changeit");
        ds.setUrl(connectURI);

        // 1. Base de dados
        SQLUserRepository userRepository = new SQLUserRepository(ds);
        TodoUserService userService = new TodoUserService(userRepository);

        // 2. Repositórios de listas e tarefas
        SQLTodoListsRepository listsRepository = new SQLTodoListsRepository(ds);
        TodoListsService toDoListService = new TodoListsService(listsRepository);
        SQLTodoRepository todoRepository = new SQLTodoRepository(ds);
        TodoService todoService = new TodoService(todoRepository, listsRepository);
        TodoListController todoListController = new TodoListController(toDoListService);
        TodoController todoController = new TodoController(todoService, toDoListService);

        // 3. Casbin Enforcer
        InputStream modelStream = App.class.getClassLoader().getResourceAsStream("model.conf");
        Model model = new Model();
        model.loadModelFromText(new String(modelStream.readAllBytes()));
        InputStream policyStream = App.class.getClassLoader().getResourceAsStream("policy.csv");
        File tempPolicy = File.createTempFile("policy", ".csv");
        tempPolicy.deleteOnExit();
        Files.copy(policyStream, tempPolicy.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Enforcer enforcer = new Enforcer(model, new FileAdapter(tempPolicy.getAbsolutePath()));

        // 4. Controllers que dependem do Enforcer — SÓ AQUI
        UserController userController = new UserController(userService, enforcer);

        AuthorizationMiddleware authMiddleware = new AuthorizationMiddleware(userRepository, enforcer);
        // CORS
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
        // Authorization middleware — excluir rotas públicas
        app.before(ctx -> {
            String path = ctx.path();
            if (path.equals("/user") || path.equals("/login")) return;
            authMiddleware.handle(ctx);
        });

        // User management
        app.post("/user", userController::createUser);
        app.get("/user/{userId}", userController::getUser);
        app.delete("/user/{userId}", userController::deleteUser);
        app.post("/login", userController::loginUser);

        // "To do" lists management
        /* POST /todolist
          {
              "listName": "Shopping list"
          }
         */
        app.post("/todolist", todoListController::createTodoList);
        app.get("/todolist", todoListController::getAllTodoLists);
        app.get("/todolist/{listId}", todoListController::getTodoList);

        // "To do" list items management
        /* POST /todo/item
          {
              "description": "Buy milk",
              "listId": 1
          }
         */
        app.post("/todo/item", todoController::createTodoItem);
        /* GET /todo/1/tasks */
        app.get("/todo/{listId}/tasks", todoController::getAllTodoItems);
        /* GET /todo/1/tasks/1 */
        app.get("/todo/{listId}/tasks/{taskId}", todoController::getTodoItem);
        /* DELETE /todo/1/tasks/1 */
        app.delete("/todo/{listId}/tasks/{taskId}", todoController::deleteTodoItem);
        /* Não é necessário porque estamos a usar DB */
        //fillDummyData(userService, toDoListService, todoService);
    }
/*
    private static void fillDummyData(
            TodoUserService userService,
            TodoListsService toDoListService,
            TodoService todoService) throws NoSuchAlgorithmException {
        userService.addUser("user1", "password1");
        userService.addUser("user2", "password2");
        toDoListService.createTodoListItem("Shopping list", 1);
        toDoListService.createTodoListItem( "Other", 1);
        todoService.createTodoItem("Bread", 1);
        todoService.createTodoItem("Milk", 1);
        todoService.createTodoItem("Eggs", 1);
        todoService.createTodoItem("Cheese", 1);
        todoService.createTodoItem("Butter", 1);
    }

 */
}