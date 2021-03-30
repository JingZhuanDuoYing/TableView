import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';
import 'package:tableview_flutter/table_view.dart';
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

  void _init() async {
    row = HeaderRow(List.generate(
        columnsCount,
        (index) => TextColumn(
              'Header$index',
              minWidth: 100,
              minHeight: 30,
              backgroundColor: Colors.white,
            )));
    row.stickyRows.addAll(List.generate(stickyRowsCount, (rowIndex) {
      return table_row.TableRow(List.generate(
          columnsCount,
          (columnIndex) => TextColumn(
                "Sticky $rowIndex - $columnIndex",
                minWidth: 100,
                minHeight: 40,
                backgroundColor: Colors.white,
              )));
    }));
    row.rows.addAll(List.generate(100, (rowIndex) {
      return table_row.TableRow(List.generate(
          columnsCount,
          (columnIndex) => TextColumn(
                "$rowIndex - $columnIndex",
                minWidth: 100,
                minHeight: 40,
                backgroundColor: Colors.white,
              )));
    }));
    specs.init(row, stickyColumnsCount);

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
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return TableView(row, specs);
  }
}
