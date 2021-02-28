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
  HeaderRow headerRow;

  @internal
  List<double> columnsWidth;
  @internal
  List<double> viewColumnsWidth;
  @internal
  List<ScrollController> controllers;
  @internal
  ScrollController scrollingController;
  @internal
  List<VoidCallback> viewColumnsWidthListener;
  @internal
  VoidCallback viewRowHeightListener;

  int stickyColumnsCount = 0;
  double defaultViewColumnsWidth = 90;

  bool stretchMode = false;
  double _averageStretchColumnWidth = 0;

  Color dividerColor;
  double dividerThickness = 1;

  bool enableRowsDivider = false;
  bool enableColumnsDivider = false;

  double offset = 0;

  TextPainter painter =
      TextPainter(maxLines: 1, textDirection: TextDirection.ltr);

  void init(HeaderRow row, int stickyColumnsCount) {
    headerRow = row;
    this.stickyColumnsCount = stickyColumnsCount;

    columnsWidth =
        List.filled(headerRow.columns.length, defaultViewColumnsWidth);
    viewColumnsWidth =
        List.filled(headerRow.columns.length, defaultViewColumnsWidth);
    controllers = List.filled(headerRow.columns.length, null);
    viewColumnsWidthListener = List.filled(headerRow.columns.length, null);
  }

  void measureTextColumn(table_row.TableRow row, TextColumn column) {
    int index = row.columns.indexOf(column);
    bool visible = isColumnVisible(index);
    if (!visible) {
      column.columnWidth = 0;
      column.columnHeight = 0;
      columnsWidth[index] = 0;
      viewColumnsWidth[index] = 0;
    } else if (null != column.width && null != column.height) {
      column.columnWidth = column.width;
      column.columnHeight = column.height;
    } else if (column.text?.isNotEmpty != true) {
      column.setMinSize();
    } else {
      painter.text =
          TextSpan(text: column.text, style: column.textStyle ?? TextStyle());
      painter.layout();
      column.setTextSize(painter.width, painter.height);
    }

    if (null == row.height) {
      double viewRowHeight = max(row.rowHeight, column.columnHeight);
      if (viewRowHeight != row.rowHeight) {
        row.rowHeight = viewRowHeight;
        viewRowHeightListener?.call();
      }
    }

    columnsWidth[index] = max(column.columnWidth, columnsWidth[index]);
    double viewColumnWidth = max(column.columnWidth, viewColumnsWidth[index]);
    if (viewColumnWidth != viewColumnsWidth[index]) {
      viewColumnsWidth[index] = viewColumnWidth;
      viewColumnsWidthListener[index]?.call();
    }
  }

  bool isColumnVisible(int index) {
    return headerRow?.columns[index]?.visible == true;
  }

  ScrollController getScrollController(int columnIndex) {
    ScrollController controller = controllers[columnIndex];
    if (null != controller) return controller;
    controller = ScrollController();
    controllers[columnIndex] = controller;
    return controller;
  }

  double getViewColumnWidth(int columnIndex) {
    return viewColumnsWidth[columnIndex];
  }

  void onScrolled() {
    var controller = scrollingController;
    if (controller?.hasClients != true) return;
    offset = controller.offset;
    controllers.forEach((element) {
      if (element == controller) return;
      if (element?.hasClients != true) return;
      if (element.offset == offset) return;
      element.jumpTo(offset);
    });
  }

  Divider getRowsDivider() {
    if (!enableRowsDivider) return null;
    return Divider(
      height: dividerThickness,
      thickness: dividerThickness,
      color: dividerColor,
    );
  }

  VerticalDivider getVerticalDivider() {
    if (!enableColumnsDivider) return null;
    return VerticalDivider(
      width: dividerThickness,
      thickness: dividerThickness,
      color: dividerColor,
    );
  }
}
