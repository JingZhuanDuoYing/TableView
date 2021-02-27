import 'package:tableview_flutter/table_column.dart';
import 'package:tableview_flutter/table_row.dart';

class HeaderRow extends TableRow {

  HeaderRow(List<TableColumn> columns) : super(columns);

  List<TableRow> stickyRows = [];
  List<TableRow> rows = [];

}