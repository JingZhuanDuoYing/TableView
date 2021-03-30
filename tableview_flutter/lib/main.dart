import 'dart:math';

import 'package:flutter/foundation.dart';
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
  final ScrollController controller =
      ScrollController(initialScrollOffset: 1050);
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
            child: Scrollable(
              controller: controller,
              axisDirection: AxisDirection.right,
              viewportBuilder: (context, offset) {
                return Viewport(
                  axisDirection: AxisDirection.right,
                  offset: offset,
                  slivers: [
                    RecyclerView(
                      SliverChildBuilderDelegate(
                        (context, index) {
                          print('12345 1 build $index');
                          return Container(
                            width: 100,
                            height: 100,
                            color: colors[random.nextInt(colors.length)],
                            alignment: Alignment.center,
                            child: Text('$index'),
                          );
                        },
                        childCount: 100,
                      ),
                    )
                  ],
                );
              },
            ),
          ),
        ),
      ),
    );
  }
}
