
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/column_scroll_listener.dart';
import 'package:tableview_flutter/header_row.dart';
import 'package:tableview_flutter/no_glow_behavior.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';
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

    widgets.add(ColumnScrollListener(
        widget.specs,
        controller,
        Expanded(
          child: Container(
            width: columnWidth,
            child: _buildListView(controller),
          ),
        )));

    return Container(
      width: widget.specs.viewColumnsWidth[widget.columnIndex],
      child: Column(children: widgets),
    );
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
