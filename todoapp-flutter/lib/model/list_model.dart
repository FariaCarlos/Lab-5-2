class TodoListModel {
  final int listId;
  final int ownerId;
  final String name;
  

  TodoListModel({required this.listId, required this.ownerId, required this.name});

  static TodoListModel fromJson(task) {
    return TodoListModel(
      listId: task['listId'] as int,
      ownerId: task['ownerId'] as int,
      name: task['name'] as String,
    );
  }

  @override
  String toString() {
    return 'TodoListModel{listId: $listId, ownerId: $ownerId, name: $name}';
  }

}