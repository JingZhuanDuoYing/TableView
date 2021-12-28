import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_view_nested_scroll_controller.dart';
import 'package:tableview_flutter/table_view_nested_scroll_view.dart';
import 'package:tableview_flutter/table_view_test_widget.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with TickerProviderStateMixin {
  final scrollController = ScrollController();
  late TableViewNestedScrollController nestedScrollController =
      TableViewNestedScrollController(scrollController);
  late TabController controller = TabController(length: 2, vsync: this);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        body: TableViewNestedScrollView(
          controller: nestedScrollController,
          headerSliverBuilder: (BuildContext context, bool innerBoxIsScrolled) {
            return <Widget>[
              SliverToBoxAdapter(
                child: Container(height: 200, color: Colors.amber),
              )
            ];
          },
          body: Stack(
            children: [
              Container(
                height: 50,
                margin: EdgeInsets.only(top: 20),
                child: TabBar(
                  tabs: [
                    Tab(
                      text: '1111111',
                    ),
                    Tab(text: '222222')
                  ],
                  labelStyle:
                      TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
                  unselectedLabelStyle:
                      TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
                  labelColor: Colors.black,
                  unselectedLabelColor: Colors.black45,
                  controller: controller,
                ),
              ),
              Padding(
                padding: EdgeInsets.only(top: 70),
                child: TabBarView(
                  controller: controller,
                  children: [
                    Container(
                      child: ListView.builder(
                        itemCount: 100,
                        itemBuilder: (BuildContext context, int index) {
                          return ListTile(
                              title: Text(
                                  '$index$index$index$index$index$index$index$index$index',
                                  style: TextStyle(
                                      fontSize: 18, color: Colors.black)));
                        },
                      ),
                    ),
                    Container(
                      child: TableViewTestWidget(
                          nestedScrollController: nestedScrollController),
                    )
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    controller.dispose();
  }
}
