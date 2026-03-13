import 'package:flutter/material.dart';
import 'package:todoapp/model/list_model.dart';
import 'package:todoapp/screens/tasks_screen.dart';
import 'package:todoapp/services/todo_service.dart';

import '../services/logger.dart';

class AllListsScreen extends StatelessWidget {
  const AllListsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    Future<List<TodoListModel>> tasks = TodoService.getAllLists();
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue,
        title: const Text('All lists of tasks'),
      ),
      body: Center(
        child: FutureBuilder(
          future: tasks,
          builder: (BuildContext context,
              AsyncSnapshot<List<TodoListModel>> snapshot) {
            if (snapshot.connectionState == ConnectionState.waiting) {
              return const CircularProgressIndicator();
            } else if (snapshot.hasError) {
              return Text('Error: ${snapshot.error}');
            } else {
              return ListView.builder(
                itemCount: snapshot.data!.length,
                itemBuilder: (BuildContext context, int index) {
                  return Card(
                    child: ListTile(
                      onTap: () {
                        LoggerService.info(
                            'Tapped on ${snapshot.data![index]}');
                        Navigator.of(context).push(MaterialPageRoute(
                          builder: (context) =>
                              TasksScreen(snapshot.data![index].listId),
                        ));
                      },
                      title: Text(snapshot.data![index].name),
                    ),
                  );
                },
              );
            }
          },
        ),
      ),
    );
  }
}
