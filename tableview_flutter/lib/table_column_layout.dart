import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/header_row.dart';
import 'package:tableview_flutter/no_glow_behavior.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';
import 'package:tableview_flutter/table_view.dart';
import 'package:tableview_flutter/table_view_def.dart';

class TableColumnLayout extends StatefulWidget {
  final TableSpecs specs;
  final HeaderRow headerRow;
  final int columnIndex;
  final bool sticky;
  final ColumnGestureDetectorCreator? columnGestureDetectorCreator;

  TableColumnLayout(this.specs, this.headerRow, this.columnIndex, this.sticky,
      this.columnGestureDetectorCreator);

  @override
  State<StatefulWidget> createState() => _TableColumnLayoutState();
}

class _TableColumnLayoutState extends State<TableColumnLayout> {
  @override
  void initState() {
    super.initState();
    if (!widget.sticky) {
      widget.specs.viewColumnsWidthListener[widget.columnIndex] = () {
        setState(() {});
      };
    }
  }

  @override
  void dispose() {
    super.dispose();
    if (!widget.sticky) {
      widget.specs.viewColumnsWidthListener[widget.columnIndex] = null;
    }
  }

  @override
  Widget build(BuildContext context) {
    double columnWidth = widget.specs.getViewColumnWidth(widget.columnIndex);

    List<Widget> widgets = [];
    var headerColumn = widget.headerRow.getColumnAt(widget.columnIndex);
    var headerColumnWidget = headerColumn.build(
        context, widget.specs, widget.headerRow, widget.columnIndex);
    var headerColumnWidgetWrapper = widget.columnGestureDetectorCreator
            ?.call(widget.headerRow, headerColumn, headerColumnWidget) ??
        headerColumnWidget;
    widgets.add(headerColumnWidgetWrapper);
    Divider? headerDivider = widget.specs.getRowsDivider();
    if (null != headerDivider) widgets.add(headerDivider);

    widget.headerRow.stickyRows.forEach((row) {
      var column = row.getColumnAt(widget.columnIndex);
      var columnWidget =
          column.build(context, widget.specs, row, widget.columnIndex);
      var columnWidgetWrapper = widget.columnGestureDetectorCreator
              ?.call(row, column, columnWidget) ??
          columnWidget;
      widgets.add(columnWidgetWrapper);
      Divider? stickyDivider = widget.specs.getRowsDivider();
      if (null != stickyDivider) widgets.add(stickyDivider);
    });

    ScrollController controller =
        widget.specs.getScrollController(widget.columnIndex);
    WidgetsBinding.instance?.addPostFrameCallback((timeStamp) {
      if (controller.hasClients) controller.jumpTo(widget.specs.offset);
    });

    widgets.add(NotificationListener<ScrollNotification>(
      onNotification: (notification) {
        if (notification is ScrollStartNotification) {
          _onScrollStart(context, notification, widget.specs, controller);
        } else if (notification is ScrollEndNotification) {
          _onScrollEnd(context, notification, widget.specs, controller);
        } else if (notification is ScrollUpdateNotification) {
          _onScrolling(context, notification, widget.specs, controller);
        }
        return true;
      },
      child: Expanded(
        child: Container(
          width: columnWidth,
          child: _buildListView(controller),
        ),
      ),
    ));

    return Container(
      width: widget.specs.viewColumnsWidth[widget.columnIndex],
      child: Column(children: widgets),
    );
  }

  void _onScrollStart(
      BuildContext context,
      ScrollStartNotification notification,
      TableSpecs specs,
      ScrollController controller) {
    if (null == specs.scrollingController) {
      specs.scrollingController = controller;
    } else if (notification.dragDetails?.kind == PointerDeviceKind.touch) {
      if (specs.scrollingController?.hasClients == true) {
        specs.scrollingController?.jumpTo(specs.offset);
      }
      specs.scrollingController = controller;
    }
  }

  void _onScrolling(BuildContext context, ScrollUpdateNotification notification,
      TableSpecs specs, ScrollController controller) {
    if (null != specs.scrollingController) {
      specs.onScrolled();
      if (specs.scrollingController!.hasClients &&
          specs.scrollingController!.position.atEdge &&
          specs.scrollingController!.position.pixels > 0) {
        context
            .findAncestorWidgetOfExactType<TableView>()
            ?.onScrollToEndListener
            ?.call();
      }
    }
  }

  void _onScrollEnd(BuildContext context, ScrollEndNotification notification,
      TableSpecs specs, ScrollController controller) {
    if (controller == specs.scrollingController) {
      specs.scrollingController = null;
      context
          .findAncestorWidgetOfExactType<TableView>()
          ?.onVerticalScrolledListener
          ?.call();
    }
  }

  Widget _buildListView(ScrollController controller) {
    var itemBuilder = (context, index) {
      table_row.TableRow row = widget.headerRow.rows[index];
      var column = row.getColumnAt(widget.columnIndex);
      var columnWidget =
          column.build(context, widget.specs, row, widget.columnIndex);
      return widget.columnGestureDetectorCreator
              ?.call(row, column, columnWidget) ??
          columnWidget;
    };
    if (widget.specs.enableRowsDivider) {
      return ScrollConfiguration(
        behavior: NoGlowBehavior(),
        child: ListView.separated(
          controller: controller,
          separatorBuilder: (context, index) => widget.specs.getRowsDivider()!,
          itemCount: widget.headerRow.rows.length,
          itemBuilder: itemBuilder,
        ),
      );
    } else {
      return ScrollConfiguration(
        behavior: NoGlowBehavior(),
        child: ListView.builder(
          controller: controller,
          itemCount: widget.headerRow.rows.length,
          itemBuilder: itemBuilder,
        ),
      );
    }
  }
}
