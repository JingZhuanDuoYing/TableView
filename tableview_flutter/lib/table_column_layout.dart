import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/column_scroll_listener.dart';
import 'package:tableview_flutter/header_row.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';

class TableColumnLayout extends StatefulWidget {
  final TableSpecs specs;
  final HeaderRow headerRow;
  final int columnIndex;
  final bool sticky;

  TableColumnLayout(this.specs, this.headerRow, this.columnIndex, this.sticky);

  @override
  State<StatefulWidget> createState() => _TableColumnLayoutState();
}

class _TableColumnLayoutState extends State<TableColumnLayout> {
  @override
  void initState() {
    super.initState();
    if (!widget.sticky) {
      widget.specs.viewColumnsWidthListener[widget.columnIndex] = () {
        _onColumnsWidthChanged();
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

  void _onColumnsWidthChanged() {
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    double columnWidth = widget.specs.getViewColumnWidth(widget.columnIndex);

    List<Widget> widgets = [];
    Widget headerColumn = widget.headerRow.columns[widget.columnIndex]
        .build(context, widget.specs, widget.headerRow, widget.columnIndex);
    widgets.add(headerColumn);
    widgets.add(widget.specs.getRowsDivider());

    widget.headerRow.stickyRows.forEach((row) {
      Widget columnWidget = row.columns[widget.columnIndex]
          .build(context, widget.specs, row, widget.columnIndex);
      widgets.add(columnWidget);
      widgets.add(widget.specs.getRowsDivider());
    });

    ScrollController controller =
        widget.specs.getScrollController(widget.columnIndex);
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
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
      return row.columns[widget.columnIndex]
          .build(context, widget.specs, row, widget.columnIndex);
    };
    if (widget.specs.enableRowsDivider) {
      return ListView.separated(
        controller: controller,
        separatorBuilder: (context, index) => widget.specs.getRowsDivider(),
        itemCount: widget.headerRow.rows.length,
        itemBuilder: itemBuilder,
      );
    } else {
      return ListView.builder(
        controller: controller,
        itemCount: widget.headerRow.rows.length,
        itemBuilder: itemBuilder,
      );
    }
  }
}
