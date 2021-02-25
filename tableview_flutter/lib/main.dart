import 'dart:ui';

import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final List<ScrollController> controllers = List.filled(15, null);
  ScrollController scrollingController;
  var offset = 0.0;

  void _onScroll(ScrollController controller) {
    offset = controller.offset;
    controllers.forEach((element) {
      if (element == controller) return;
      if (element?.hasClients != true) return;
      if (element.offset == offset) return;
      element.jumpTo(offset);
    });
  }

  @override
  Widget build(BuildContext context) {
    final title = 'Long List';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: Container(
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: 15,
            itemBuilder: (context, index) {
              var controller = ScrollController(initialScrollOffset: offset);
              controllers[index] = controller;
              return Container(
                width: 140,
                child: NotificationListener<ScrollNotification>(
                    onNotification: (ScrollNotification notification) {
                      if (notification is ScrollStartNotification) {
                        if (null == scrollingController) {
                          scrollingController = controller;
                        } else if (notification?.dragDetails?.kind ==
                            PointerDeviceKind.touch) {
                          if (scrollingController?.hasClients == true) {
                            scrollingController?.jumpTo(offset);
                          }
                          scrollingController = controller;
                        }
                      } else if (notification is ScrollEndNotification) {
                        if (controller == scrollingController) {
                          scrollingController = null;
                        }
                      } else if (notification is ScrollUpdateNotification) {
                        if (null != scrollingController) {
                          _onScroll(scrollingController);
                        }
                      }
                      return true;
                    },
                    child: ListView.builder(
                        controller: controller,
                        itemCount: 50,
                        itemBuilder: (context, columnIndex) {
                          return Container(
                            height: 40,
                            width: 140,
                            alignment: Alignment.center,
                            child: Text('Row: $index, column: $columnIndex'),
                          );
                        })),
              );
            },
          ),
        ),
      ),
    );
  }
}
