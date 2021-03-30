import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/recycler_view.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        body: Container(
          color: Colors.cyan,
          child: Container(
            height: 100,
            child: ColumnListView(),
          ),
        ),
      ),
    );
  }
}

class ColumnListView extends RecyclerView {
  final List<Color> colors = [
    Colors.black,
    Colors.black26,
    Colors.lightGreenAccent,
    Colors.lightGreen,
    Colors.amber,
    Colors.amberAccent,
    Colors.blue,
    Colors.blueAccent,
    Colors.red,
    Colors.redAccent,
    Colors.purple,
    Colors.purpleAccent
  ];
  final Random random = Random();

  @override
  Widget? buildChild(BuildContext context, int index) {
    return Container(
      width: 100,
      height: 100,
      color: colors[random.nextInt(colors.length)],
      alignment: Alignment.center,
      child: Text('$index'),
    );
  }

  @override
  AxisDirection getAxisDirection() => AxisDirection.right;

  @override
  int getChildCount() => 100;

  @override
  ScrollController? createScrollController() =>
      ScrollController(initialScrollOffset: 1024);

  @override
  double getChildMainAxisLayoutOffsetAtIndex(int index) => 100.0 * index;

  @override
  double getChildMainAxisSizeAtIndex(int index) => 100;
}
