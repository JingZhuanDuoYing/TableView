import 'package:tableview_flutter/table_specs.dart';

import 'table_column.dart';
import 'table_row.dart';

class HeaderRow extends TableRow {
  HeaderRow(List<TableColumn> columns) : super(columns);

  List<TableRow> stickyRows = [];
  List<TableRow> rows = [];

  Future<void> measure(TableSpecs specs) async {
    columns.forEach((element) => specs.measureColumn(this, element));
    stickyRows.forEach((row) {
      row.columns.forEach((column) => specs.measureColumn(row, column));
    });
    rows.forEach((row) {
      row.columns.forEach((column) => specs.measureColumn(row, column));
    });
  }
}
