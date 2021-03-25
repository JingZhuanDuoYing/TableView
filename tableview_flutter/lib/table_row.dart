import 'package:meta/meta.dart';
import 'package:tableview_flutter/table_column.dart';

class TableRow {
  final List<TableColumn> columns;

  double? height;
  double minHeight = 0;

  @internal
  late double rowHeight;

  TableRow(this.columns) {
    this.rowHeight = height ?? 0;
  }

  TableColumn getColumnAt(int index) {
    return columns[index];
  }

  double getRowHeight() => rowHeight;

}
