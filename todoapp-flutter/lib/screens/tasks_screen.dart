import 'package:flutter/material.dart';
import 'package:todoapp/model/todo_model.dart';
import 'package:todoapp/services/logger.dart';
import 'package:todoapp/services/todo_service.dart';

class TasksScreen extends StatelessWidget {
  final int listId;
  const TasksScreen(this.listId, {super.key});

  @override
  Widget build(BuildContext context) {
    Future<List<TodoModel>> tasks = TodoService.getAllTasks(listId);
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue,
        title: Text('List of TASKS <$listId>'),
      ),
      body: FutureBuilder(
        future: tasks, 
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const CircularProgressIndicator();
          } else if (snapshot.hasError) {
            return Text('Error: ${snapshot.error}');
          } else {
            return ListView.builder(
              itemCount: snapshot.data!.length,
              itemBuilder: (context, index) {
                TodoModel task = snapshot.data![index];
                LoggerService.info('Task: $task');
                return ListTile(
                  title: Text(task.description),
                  subtitle: Text(task.completed.toString()),
                );
              },
            );
          }
        },
      )
    );
  }
}