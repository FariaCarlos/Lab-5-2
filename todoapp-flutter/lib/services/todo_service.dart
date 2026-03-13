import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:todoapp/model/list_model.dart';
import 'package:todoapp/model/todo_model.dart';
import 'package:todoapp/services/logger.dart';
import 'package:todoapp/services/token.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

class TodoService {
  static String host = dotenv.env['HOST'] ?? 'http://localhost';
  static String port = dotenv.env['PORT'] ?? '7100';

  static Future<String?> login(String username, String password) async {
    try {
      http.Response resp = await http.post(Uri.parse('$host:$port/login'),
          headers: <String, String>{
            'Content-Type': 'application/json; charset=UTF-8',
          },
          body: jsonEncode(<String, String>{
            'username': username,
            'password': password,
          }));
      LoggerService.info(resp.body);
      if (resp.statusCode == 200) {
        // Guarda o token para uso posterior
        await TokenWrapper.setToken(username, resp.body);
        return resp.body;
      }
    } catch (e) {
      LoggerService.error(e);
    }
    return null;
  }

  static Future<List<TodoListModel>> getAllLists() async {
    // 3.b)
    String token = await TokenWrapper.getTokenAsync();
    Uri uri = Uri.parse('$host:$port/todolist');
    LoggerService.info('Calling $uri with token <$token>');
    try {
      http.Response resp = await http.get(
        uri,
        headers: {
          HttpHeaders.authorizationHeader: "$token",
        },
      );
      LoggerService.info('Response: ${resp.body}');
      if (resp.statusCode == 200) {
        List<dynamic> lists = jsonDecode(resp.body);
        return lists.map((list) => TodoListModel.fromJson(list)).toList();
      }
    } catch (e) {
      LoggerService.info(e);
    }
    return List.empty();
  }

  static Future<List<TodoModel>> getAllTasks(int listId) async {
    // 3.b)
    String token = await TokenWrapper.getTokenAsync();
    Uri uri = Uri.parse('$host:$port/todo/$listId/tasks');
    LoggerService.info('Calling $uri with token <$token>');
    try {
      http.Response resp = await http.get(
        uri,
        headers: {
          HttpHeaders.authorizationHeader: "$token",
        },
      );
      if (resp.statusCode == 200) {
        List<dynamic> tasks = jsonDecode(resp.body);
        return tasks.map((task) => TodoModel.fromJson(task)).toList();
      }
    } catch (e) {
      LoggerService.info(e);
    }
    return List.empty();
  }
}