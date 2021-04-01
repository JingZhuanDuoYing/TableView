import 'dart:ui';

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/recycler_view.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';
import 'package:tableview_flutter/table_view_def.dart';

class TableRowWidget extends RecyclerView {
  final table_row.TableRow row;
  final TableSpecs specs;
  final ColumnGestureDetectorCreator? gestureDetectorCreator;
  final OnScrollNotificationListener scrollNotificationListener;

  final int index;
  final bool sticky;
  final bool header;

  TableRowWidget(this.row, this.specs, this.gestureDetectorCreator,
      this.scrollNotificationListener, this.index, this.sticky, this.header);

  @override
  State<StatefulWidget> createState() => TableRowWidgetState();
}

class TableRowWidgetState extends RecyclerViewState<TableRowWidget> {
  ScrollController? controller;

  @override
  AxisDirection getAxisDirection() => AxisDirection.right;

  @override
  int getChildCount() =>
      widget.row.columns.length - widget.specs.stickyColumnsCount;

  @override
  ScrollController? getScrollController() {
    if (null == controller) controller = widget.specs.acquireController();
    return controller;
  }

  @override
  void dispose() {
    super.dispose();
    if (null != controller) widget.specs.releaseController(controller!);
  }

  @override
  Widget build(BuildContext context) {
    WidgetsBinding.instance?.addPostFrameCallback((timeStamp) {
      if (null != controller && controller!.hasClients)
        controller!.jumpTo(widget.specs.offset);
    });
    return NotificationListener<ScrollNotification>(
      onNotification: (notification) {
        if (notification is ScrollStartNotification) {
          _onScrollStart(context, notification);
        } else if (notification is ScrollEndNotification) {
          _onScrollEnd(context, notification);
        } else if (notification is ScrollUpdateNotification) {
          _onScrolling(context, notification);
        }
        return true;
      },
      child: super.build(context),
    );
  }

  @override
  Widget buildViewport(BuildContext context, ViewportOffset offset) {
    var stickyContentWidth = .0;
    for (var i = 0; i < widget.specs.stickyColumnsCount; i++) {
      stickyContentWidth += widget.specs.getViewColumnWidth(i);
    }

    return Container(
      height: widget.row.rowHeight,
      child: Stack(
        children: [
          Container(
            width: stickyContentWidth,
            child: Row(
              children: [
                for (var i = 0; i < widget.specs.stickyColumnsCount; i++)
                  buildColumnWidget(context, i)
              ],
            ),
          ),
          Align(
            child: Container(
              padding: EdgeInsets.only(left: stickyContentWidth),
              child: super.buildViewport(context, offset),
            ),
          )
        ],
      ),
    );
  }

  @override
  Widget? buildChild(BuildContext context, int index) {
    var columnIndex = index + widget.specs.stickyColumnsCount;
    return buildColumnWidget(context, columnIndex);
  }

  Widget buildColumnWidget(BuildContext context, int index) {
    var column = widget.row.columns[index];
    var columnWidget = column.build(context, widget.specs, widget.row, index);
    if (null == widget.gestureDetectorCreator) return columnWidget;
    return widget.gestureDetectorCreator!(
        widget.row, widget.row.columns[index], columnWidget);
  }

  @override
  double getChildMainAxisLayoutOffsetAtIndex(int index) {
    var offset = .0;
    for (var i = 0; i < index; i++) {
      var columnIndex = widget.specs.stickyColumnsCount + i;
      if (columnIndex >= widget.row.columns.length) continue;
      offset += widget.specs.getViewColumnWidth(columnIndex);
    }
    return offset;
  }

  @override
  double getChildMainAxisSizeAtIndex(int index) {
    var columnIndex = widget.specs.stickyColumnsCount + index;
    if (columnIndex >= widget.row.columns.length) return 0;
    return widget.specs.getViewColumnWidth(columnIndex);
  }

  void _onScrollStart(
      BuildContext context, ScrollStartNotification notification) {
    if (null == widget.specs.scrollingController) {
      widget.specs.scrollingController = controller;
      widget.scrollNotificationListener(notification);
    } else if (notification.dragDetails?.kind == PointerDeviceKind.touch) {
      if (widget.specs.scrollingController?.hasClients == true) {
        widget.specs.scrollingController?.jumpTo(widget.specs.offset);
      }
      widget.specs.scrollingController = controller;
      widget.scrollNotificationListener(notification);
    }
  }

  void _onScrolling(
      BuildContext context, ScrollUpdateNotification notification) {
    if (null != widget.specs.scrollingController) {
      widget.specs.onScrolled();
      widget.scrollNotificationListener(notification);
    }
  }

  void _onScrollEnd(BuildContext context, ScrollEndNotification notification) {
    if (controller == widget.specs.scrollingController) {
      widget.specs.scrollingController = null;
      widget.scrollNotificationListener(notification);
    }
  }
}
