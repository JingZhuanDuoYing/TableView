import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/recycler_view.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';

class TableRowWidget extends RecyclerView {
  final table_row.TableRow row;
  final TableSpecs specs;
  final ScrollController controller;

  TableRowWidget(this.row, this.specs, this.controller);

  Widget buildColumnWidget(BuildContext context, int index) {
    var column = row.columns[index];
    return column.build(context, specs, row, index);
  }

  @override
  AxisDirection getAxisDirection() => AxisDirection.right;

  @override
  int getChildCount() => row.columns.length - specs.stickyColumnsCount;

  @override
  ScrollController? createScrollController() => controller;

  @override
  Widget buildViewport(BuildContext context, ViewportOffset offset) {
    var stickyContentWidth = .0;
    for (var i = 0; i < specs.stickyColumnsCount; i++) {
      stickyContentWidth += specs.getViewColumnWidth(i);
    }

    return Container(
      height: row.rowHeight,
      child: Stack(
        children: [
          Container(
            width: stickyContentWidth,
            child: Row(
              children: [
                for (var i = 0; i < specs.stickyColumnsCount; i++)
                  row.columns[i].build(context, specs, row, i)
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
    var columnIndex = index + specs.stickyColumnsCount;
    return buildColumnWidget(context, columnIndex);
  }

  @override
  double getChildMainAxisLayoutOffsetAtIndex(int index) {
    var offset = .0;
    for (var i = 0; i < index; i++) {
      var columnIndex = specs.stickyColumnsCount + index;
      offset += specs.getViewColumnWidth(columnIndex);
    }
    return offset;
  }

  @override
  double getChildMainAxisSizeAtIndex(int index) {
    var columnIndex = specs.stickyColumnsCount + index;
    return specs.getViewColumnWidth(columnIndex);
  }

  Widget buildFallbackWidget(BuildContext context, int index) {
    return Container(
      width: specs.viewColumnsWidth[index],
      height: row.rowHeight,
    );
  }
}
