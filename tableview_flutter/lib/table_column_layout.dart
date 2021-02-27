import 'dart:ui';

import 'package:flutter/widgets.dart';
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
    List<Widget> widgets = [];
    Widget headerColumn = widget.headerRow.columns[widget.columnIndex]
        .build(context, widget.specs, widget.headerRow, widget.columnIndex);
    widgets.add(headerColumn);
    List<Widget> stickyColumns = widget.headerRow.stickyRows
        .map((row) => row.columns[widget.columnIndex]
            .build(context, widget.specs, row, widget.columnIndex))
        .toList(growable: false);
    widgets.addAll(stickyColumns);

    ScrollController controller =
        widget.specs.getScrollController(widget.columnIndex);
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      if (controller.hasClients) controller.jumpTo(widget.specs.offset);
    });

    widgets.add(NotificationListener<ScrollNotification>(
      onNotification: (ScrollNotification notification) {
        if (notification is ScrollStartNotification) {
          if (null == widget.specs.scrollingController) {
            widget.specs.scrollingController = controller;
          } else if (notification?.dragDetails?.kind ==
              PointerDeviceKind.touch) {
            if (widget.specs.scrollingController?.hasClients == true) {
              widget.specs.scrollingController?.jumpTo(widget.specs.offset);
            }
            widget.specs.scrollingController = controller;
          }
        } else if (notification is ScrollEndNotification) {
          if (controller == widget.specs.scrollingController) {
            widget.specs.scrollingController = null;
          }
        } else if (notification is ScrollUpdateNotification) {
          if (null != widget.specs.scrollingController) {
            widget.specs.onScrolled();
          }
        }
        return true;
      },
      child: Expanded(
        child: Container(
          width: widget.specs.getViewColumnWidth(widget.columnIndex),
          child: ListView.builder(
            controller: controller,
            itemCount: widget.headerRow.rows.length,
            itemBuilder: (context, index) {
              table_row.TableRow row = widget.headerRow.rows[index];
              return row.columns[widget.columnIndex]
                  .build(context, widget.specs, row, widget.columnIndex);
            },
          ),
        ),
      ),
    ));

    return Container(
      child: Column(children: widgets),
    );
  }
}
