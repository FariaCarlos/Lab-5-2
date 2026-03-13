import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:todoapp/services/logger.dart';
import 'package:todoapp/services/todo_service.dart';
import 'package:todoapp/services/token.dart';

class LoginInfo {
  final String username;
  final String token;

  LoginInfo(this.username, this.token);
}

class Login extends StatefulWidget {
  final Function() onLoginSuccess;
  const Login({super.key, required this.onLoginSuccess});

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {
  final _fieldUsernameController = TextEditingController();
  final _fieldPasswordController = TextEditingController();
  final Future<SharedPreferences> _prefs = SharedPreferences.getInstance();
  late Future<String> _username;

  @override
  void initState() {
    super.initState();
    _username = TokenWrapper.getUserAsync();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      //title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter Demo'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: TextField(
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    labelText: 'Enter your name',
                  ),
                  controller: _fieldUsernameController,
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: TextField(
                  obscureText: true,
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                    labelText: 'Enter your password',
                  ),
                  controller: _fieldPasswordController,
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: FloatingActionButton(
                  child: const Text('Login'),
                  onPressed: () {
                    loginAndSaveResult();
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: FloatingActionButton(
                  child: const Text('Logout'),
                  onPressed: () {
                    setState(() {
                      _username = Future.value('');
                      _prefs.then((SharedPreferences prefs) {
                        prefs.remove('user');
                        prefs.remove('token');
                      });
                    });
                  },
                ),
              ),
              FutureBuilder(
                future: _username,
                builder:
                    (BuildContext context, AsyncSnapshot<String> snapshot) {
                  if (snapshot.connectionState == ConnectionState.done) {
                    return LoginStatus(snapshot.data!);
                  } else {
                    return const CircularProgressIndicator();
                  }
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  void loginAndSaveResult() async {
    String usr = _fieldUsernameController.text;
    String pwd = _fieldPasswordController.text;
    LoggerService.info('Username: $usr, Password: $pwd');
    String? result = await TodoService.login(usr, pwd);
    LoggerService.info('Result: $result');
    if (result == null) {
      TokenWrapper.clear();
    } else {
      // store for other widgets to use when accessing the Tasks server
      TokenWrapper.setToken(usr, result);
      widget.onLoginSuccess();
    }
    setState(() {
      _username = Future.value(usr);
    });
  }
}

class LoginStatus extends StatelessWidget {
  final String _username;

  const LoginStatus(this._username, {super.key});

  @override
  Widget build(BuildContext context) {
    
    return _username == ''
        ? Card(
          elevation: 10,
          shadowColor: Colors.grey,
          child: Container(
              color: Colors.red,
              child: const Text(
                'You have not logged in yet',
              ),
            ),
        )
        : Container(
            color: Colors.green,
            child: Text(
              'Last logged in user: $_username',
            ),
          );
  }
}
