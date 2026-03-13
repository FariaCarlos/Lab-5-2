class TodoModel {
  final int id;
  final int listId;
  final String description;
  final bool completed;

  TodoModel({required this.id, required this.listId, required this.description, required this.completed});

  static TodoModel fromJson(dynamic task) {
    return TodoModel(
      id: task['id'],
      listId: task['listId'],
      description: task['description'],
      completed: task['completed'],
    );
  }

  @override
  String toString() {
    return 'TodoModel{id: $id, listId: $listId, description: $description, completed: $completed}';
  }

}