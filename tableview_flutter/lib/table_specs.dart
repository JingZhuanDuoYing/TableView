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
  late List<double> columnsWidth;
  @internal
  late List<double> viewColumnsWidth;
  @internal
  late List<ScrollController?> controllers;
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

  void measureColumn(table_row.TableRow row, TableColumn column) {
    int index = row.columns.indexOf(column);
    bool visible = isColumnVisible(index);
    if (!visible) {
      column.columnWidth = 0;
      column.columnHeight = 0;
      columnsWidth[index] = 0;
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

    columnsWidth[index] = max(column.columnWidth, columnsWidth[index]);
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
    ScrollController? controller = controllers[columnIndex];
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
    if (null == controller || controller.hasClients != true) return;
    offset = controller.offset;
    controllers.forEach((element) {
      if (element == controller) return;
      if (null == element || element.hasClients != true) return;
      if (element.offset == offset) return;
      element.jumpTo(offset);
    });
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
