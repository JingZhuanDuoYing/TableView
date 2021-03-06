import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_column.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;

typedef ColumnGestureDetectorCreator = GestureDetector Function(
    table_row.TableRow row, TableColumn column, Widget columnWidget);

typedef OnScrollNotificationListener = void Function(
    ScrollNotification notification);

typedef TableViewScrollStateListener = void Function(
    Axis axis, TableViewScrollState state);

enum TableViewScrollState { START, SCROLLING, END }
