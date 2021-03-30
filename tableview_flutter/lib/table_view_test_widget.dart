import 'dart:ffi';

import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';
import 'package:tableview_flutter/text_column.dart';

import 'header_row.dart';

class TableViewTestWidget extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _TableViewTestState();
}

class _TableViewTestState extends State<TableViewTestWidget> {
  late HeaderRow row;
  TableSpecs specs = TableSpecs();
  final int stickyRowsCount = 2;
  final int columnsCount = 30;
  final int stickyColumnsCount = 1;

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<Void> _init() async {
    row = HeaderRow(
        List.generate(columnsCount, (index) => TextColumn('Header$index')));
    row.stickyRows.addAll(List.generate(stickyRowsCount, (rowIndex) {
      return table_row.TableRow(List.generate(columnsCount,
          (columnIndex) => TextColumn("Sticky $rowIndex - $columnIndex")));
    }));
    row.rows.addAll(List.generate(100, (rowIndex) {
      return table_row.TableRow(List.generate(columnsCount,
          (columnIndex) => TextColumn("$rowIndex - $columnIndex")));
    }));
    specs.init(row, stickyColumnsCount);
    await _measure(row);
    return Future.value();
  }

  _measure(HeaderRow row) async {
    row.columns.forEach((element) {
      specs.measureColumn(row, element);
    });
    row.stickyRows.forEach((element) {
      element.columns.forEach((column) {
        specs.measureColumn(element, column);
      });
    });
    row.rows.forEach((element) {
      element.columns.forEach((column) {
        specs.measureColumn(element, column);
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    throw UnimplementedError();
  }
}
