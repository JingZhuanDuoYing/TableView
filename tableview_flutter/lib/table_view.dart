import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/no_glow_behavior.dart';
import 'package:tableview_flutter/table_row_widget.dart';
import 'package:tableview_flutter/table_view_def.dart';
import 'package:tableview_flutter/table_view_nested_scroll_controller.dart';

import 'header_row.dart';
import 'table_specs.dart';

typedef TableViewOnNestedParentScrolledNotifier = void Function(
    double currentOffset, double maxScrollExtent);

class TableView extends StatefulWidget {
  final HeaderRow headerRow;
  final TableSpecs specs;
  final ColumnGestureDetectorCreator? columnGestureDetectorCreator;
  final TableViewScrollStateListener? scrollStateListener;
  final VoidCallback? onScrollToEndListener;
  final ScrollController? scrollController;
  final ScrollPhysics? scrollPhysics;
  final TableViewNestedScrollController? nestedScrollController;

  TableView(this.headerRow, this.specs,
      {this.scrollController,
      this.columnGestureDetectorCreator,
      this.onScrollToEndListener,
      this.scrollStateListener,
      this.scrollPhysics,
      this.nestedScrollController});

  @override
  State<StatefulWidget> createState() => _TableViewState();
}

class _TableViewState extends State<TableView> {
  bool _scrollingHorizontally = false;
  bool _scrollingVertically = false;
  late ScrollController controller =
      widget.nestedScrollController?.tableViewScrollController ??
          widget.scrollController ??
          ScrollController();
  late ScrollPhysics? scrollPhysics =
      widget.scrollPhysics ?? widget.nestedScrollController?.tableViewScrollPhysics;

  @override
  void initState() {
    super.initState();
    if (widget.specs.stickyColumnsCount > 0) {
      for (var i = 0; i < widget.specs.stickyColumnsCount; i++) {
        widget.specs.viewColumnsWidthListener[i] = () {
          setState(() {});
        };
      }
    }
  }

  @override
  void dispose() {
    super.dispose();
    if (widget.specs.stickyColumnsCount > 0) {
      for (var i = 0; i < widget.specs.stickyColumnsCount; i++) {
        widget.specs.viewColumnsWidthListener[i] = null;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (widget.specs.screenWidth <= 0) {
      widget.specs.screenWidth = MediaQuery.of(context).size.width;
    }
    var headerRowHeight = widget.headerRow.rowHeight;
    var stickyRowsHeight = .0;
    for (var i = 0; i < widget.headerRow.stickyRows.length; i++) {
      stickyRowsHeight += widget.headerRow.stickyRows[i].rowHeight;
    }
    return Stack(
      children: [
        ScrollConfiguration(
          behavior: NoGlowBehavior(),
          child: ListView.builder(
            shrinkWrap: true,
            itemCount: widget.headerRow.stickyRows.length + 1,
            itemBuilder: (context, index) {
              if (index == 0) {
                return TableRowWidget(
                    widget.headerRow,
                    widget.specs,
                    widget.columnGestureDetectorCreator,
                    (notification) => _onHorizontalScrollCallback(notification),
                    0,
                    true,
                    true);
              } else {
                return TableRowWidget(
                    widget.headerRow.stickyRows[index - 1],
                    widget.specs,
                    widget.columnGestureDetectorCreator,
                    (notification) => _onHorizontalScrollCallback(notification),
                    index - 1,
                    true,
                    false);
              }
            },
          ),
        ),
        Padding(
          padding: EdgeInsets.only(top: headerRowHeight + stickyRowsHeight),
          child: ScrollConfiguration(
            behavior: NoGlowBehavior(),
            child: NotificationListener<ScrollNotification>(
              onNotification: (notification) =>
                  _onVerticalScrollCallback(notification),
              child: ListView.builder(
                controller: controller,
                physics: scrollPhysics,
                itemCount: widget.headerRow.rows.length,
                itemBuilder: (context, index) {
                  return TableRowWidget(
                    widget.headerRow.rows[index],
                    widget.specs,
                    widget.columnGestureDetectorCreator,
                        (notification) =>
                        _onHorizontalScrollCallback(notification),
                    index,
                    false,
                    false,
                  );
                },
              ),
            ),
          ),
        )
      ],
    );
  }

  bool _onHorizontalScrollCallback(ScrollNotification notification) {
    if (notification is ScrollStartNotification) {
      if (_scrollingVertically) return true;
      _scrollingHorizontally = true;
      _scrollingVertically = false;
      widget.scrollStateListener
          ?.call(Axis.horizontal, TableViewScrollState.START);
    } else if (notification is ScrollUpdateNotification) {
      if (_scrollingHorizontally)
        widget.scrollStateListener
            ?.call(Axis.horizontal, TableViewScrollState.SCROLLING);
    } else if (notification is ScrollEndNotification) {
      if (_scrollingHorizontally) {
        // delay listener callback because this end notification may be triggered another horizontal scroll
        Timer(Duration(milliseconds: 8), () {
          if (!_scrollingHorizontally) {
            widget.scrollStateListener
                ?.call(Axis.horizontal, TableViewScrollState.END);
          }
        });
      }
      _scrollingHorizontally = false;
    }
    return true;
  }

  bool _onVerticalScrollCallback(ScrollNotification notification) {
    if (controller.hasClients) widget.specs.verticalOffset = controller.offset;
    if (notification is ScrollStartNotification) {
      if (_scrollingHorizontally) return true;
      _scrollingHorizontally = false;
      _scrollingVertically = true;
      widget.scrollStateListener
          ?.call(Axis.vertical, TableViewScrollState.START);
    } else if (notification is ScrollUpdateNotification) {
      if (_scrollingVertically &&
          controller.hasClients &&
          controller.position.atEdge &&
          controller.position.pixels > 0) {
        widget.onScrollToEndListener?.call();
        _scrollingVertically = false;
      }
      if (_scrollingVertically) {
        widget.scrollStateListener
            ?.call(Axis.vertical, TableViewScrollState.SCROLLING);
      }
    } else if (notification is ScrollEndNotification) {
      if (_scrollingVertically) {
        // delay listener callback because this end notification may be triggered another vertical scroll
        Timer(Duration(milliseconds: 8), () {
          if (!_scrollingVertically) {
            widget.scrollStateListener
                ?.call(Axis.vertical, TableViewScrollState.END);
          }
        });
      }
      widget.nestedScrollController?.onTableViewScrollEnd(controller);
      _scrollingVertically = false;
    }
    return true;
  }
}
