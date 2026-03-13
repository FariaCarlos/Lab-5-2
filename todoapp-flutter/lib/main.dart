import 'package:flutter/material.dart';
import 'package:todoapp/screens/login_screen.dart';
import 'package:todoapp/screens/lists_screen.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

void main() async {
  await dotenv.load();
  runApp(const MainApp());
}

class MainApp extends StatefulWidget {
  const MainApp({super.key});

  @override
  State<MainApp> createState() => _MainAppState();
}

class _MainAppState extends State<MainApp> {
  int _selectedIndex = 0;
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Task App',
      home: Scaffold(
        body: [
          Login(onLoginSuccess: () {
            setState(() {
              _selectedIndex = 1;
            });
          }),
          const AllListsScreen(),
        ][_selectedIndex],
        bottomNavigationBar: NavigationBar(
          selectedIndex: _selectedIndex,
          onDestinationSelected: (int index) {
            setState(() {
              _selectedIndex = index;
            });
          },
          destinations: const [
            NavigationDestination(
              label: 'Login',
              icon: Icon(Icons.login),
            ),
            NavigationDestination(
              label: 'Tasks',
              icon: Icon(Icons.task),
            ),
          ],
        ),
      ),
    );
  }
}
