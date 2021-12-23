import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_view_nested_scroll_controller.dart';
import 'package:tableview_flutter/table_view_test_widget.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var scrollController = ScrollController();
  late var tableViewNestedScrollController =
      TableViewNestedScrollController(scrollController);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        body: NotificationListener<ScrollEndNotification>(
          onNotification: (notification) {
            if (notification is ScrollEndNotification) {
              tableViewNestedScrollController.onNestedViewScrollEnd();
            }
            return true;
          },
          child: NestedScrollView(
            controller: scrollController,
            headerSliverBuilder:
                (BuildContext context, bool innerBoxIsScrolled) {
              return <Widget>[
                SliverToBoxAdapter(
                  child: Container(height: 200, color: Colors.amber),
                )
              ];
            },
            body: Container(
              child: TableViewTestWidget(
                  nestedScrollController: tableViewNestedScrollController),
            ),
          ),
        ),
      ),
    );
  }
}
