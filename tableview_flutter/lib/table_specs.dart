import 'dart:core';
import 'dart:math';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:meta/meta.dart';
import 'package:tableview_flutter/table_column.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/text_column.dart';

import 'header_row.dart';

class TableSpecs {
  late HeaderRow headerRow;

  @internal
  late List<double> viewColumnsWidth;
  var _controllers = Set();
  var _idleControllers = Set();

  @internal
  ScrollController? scrollingController;
  @internal
  late List<VoidCallback?> viewColumnsWidthListener;
  @internal
  VoidCallback? viewRowHeightListener;

  int stickyColumnsCount = 0;
  double defaultViewColumnsWidth = 90;

  bool stretchMode = false;
  double _averageStretchColumnWidth = 0;

  Color? dividerColor;
  double dividerThickness = 1;

  bool enableRowsDivider = false;
  bool enableColumnsDivider = false;

  double horizontalOffset = 0;
  double verticalOffset = 0;

  TextPainter painter =
      TextPainter(maxLines: 1, textDirection: TextDirection.ltr);

  void init(HeaderRow row, int stickyColumnsCount) {
    headerRow = row;
    this.stickyColumnsCount = stickyColumnsCount;

    viewColumnsWidth =
        List.filled(headerRow.columns.length, defaultViewColumnsWidth);
    viewColumnsWidthListener = List.filled(headerRow.columns.length, null);
  }

  void measureColumn(table_row.TableRow row, TableColumn column) {
    int index = row.columns.indexOf(column);
    bool visible = isColumnVisible(index);
    if (!visible) {
      column.columnWidth = 0;
      column.columnHeight = 0;
      viewColumnsWidth[index] = 0;
    } else if (null != column.width && null != column.height) {
      column.columnWidth = column.width!;
      column.columnHeight = column.height!;
    } else if (column is TextColumn) {
      if (null == column.text || column.text!.isEmpty) {
        column.setMinSize();
      } else {
        painter.text = TextSpan(text: column.text, style: column.textStyle);
        painter.layout();
        column.setTextSize(painter.width, painter.height);
      }
    } else {
      double columnWidth = column.width ?? 0;
      column.columnWidth =
          [columnWidth, column.minWidth, viewColumnsWidth[index]].reduce(max);
      double columnHeight = column.height ?? 0;
      column.columnHeight =
          [columnHeight, column.minHeight, row.rowHeight].reduce(max);
    }

    if (null == row.height) {
      double viewRowHeight = max(row.rowHeight, column.columnHeight);
      if (viewRowHeight != row.rowHeight) {
        row.rowHeight = viewRowHeight;
        viewRowHeightListener?.call();
      }
    }

    double viewColumnWidth = max(column.columnWidth, viewColumnsWidth[index]);
    if (viewColumnWidth != viewColumnsWidth[index]) {
      viewColumnsWidth[index] = viewColumnWidth;
      viewColumnsWidthListener[index]?.call();
    }
  }

  bool isColumnVisible(int index) {
    return headerRow.columns.elementAt(index).visible == true;
  }

  ScrollController getScrollController(int columnIndex) {
    // ScrollController? controller = controllers[columnIndex];
    // if (null != controller) return controller;
    // controller = ScrollController();
    // controllers[columnIndex] = controller;
    // return controller;
    return ScrollController();
  }

  ScrollController acquireController() {
    if (_idleControllers.isNotEmpty) {
      var controller = _idleControllers.first;
      if (_idleControllers.remove(controller)) {
        _controllers.add(controller);
        return controller;
      }
    }

    var controller = ScrollController(initialScrollOffset: horizontalOffset);
    _controllers.add(controller);
    return controller;
  }

  void releaseController(ScrollController controller) {
    if (!_controllers.remove(controller)) return;
    _idleControllers.add(controller);
  }

  void releaseIdleControllers() {
    var iterator = _controllers.iterator;
    var idles = Set();
    while (iterator.moveNext()) {
      ScrollController controller = iterator.current;
      if (!controller.hasClients) idles.add(controller);
    }
    _controllers.removeAll(idles);
    _idleControllers.addAll(idles);
  }

  double getViewColumnWidth(int columnIndex) {
    return viewColumnsWidth[columnIndex];
  }

  void onScrolled() {
    var controller = scrollingController;
    if (null == controller || controller.hasClients != true) return;
    horizontalOffset = controller.offset;
    var iterator = _controllers.iterator;
    while (iterator.moveNext()) {
      ScrollController current = iterator.current;
      if (current == controller) continue;
      if (!current.hasClients) continue;
      if (current.offset == horizontalOffset) continue;
      current.jumpTo(horizontalOffset);
    }
  }

  Divider? getRowsDivider() {
    if (!enableRowsDivider) return null;
    return Divider(
      height: dividerThickness,
      thickness: dividerThickness,
      color: dividerColor,
    );
  }

  VerticalDivider? getVerticalDivider() {
    if (!enableColumnsDivider) return null;
    return VerticalDivider(
      width: dividerThickness,
      thickness: dividerThickness,
      color: dividerColor,
    );
  }
}
