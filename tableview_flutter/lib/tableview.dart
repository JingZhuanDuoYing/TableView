import 'package:flutter/widgets.dart';

class TableView extends StatefulWidget {

  @override
  State<StatefulWidget> createState() => _TableViewState();

}

class _TableViewState extends State {

  final int columnsCount = 20;
  final int stickyColumnsCount = 1;
  final int stickyRowsCount = 1;
  final int rowsCount = 100;

  @override
  Widget build(BuildContext context) {
    return Container(
      child: ListView(),
    );
  }

}