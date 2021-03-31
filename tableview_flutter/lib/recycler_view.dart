import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/recycler_list.dart';

abstract class RecyclerView extends StatefulWidget {}

abstract class RecyclerViewState<T extends RecyclerView> extends State<T> {
  @override
  Widget build(BuildContext context) {
    return Scrollable(
      axisDirection: getAxisDirection(),
      controller: getScrollController(),
      viewportBuilder: (context, offset) => buildViewport(context, offset),
    );
  }

  Widget buildViewport(BuildContext context, ViewportOffset offset) {
    return Viewport(
      axisDirection: getAxisDirection(),
      offset: offset,
      slivers: [
        RecyclerList(
          SliverChildBuilderDelegate(
            (context, index) => buildChild(context, index),
            childCount: getChildCount(),
          ),
          (index) => getChildMainAxisSizeAtIndex(index),
          (index) => getChildMainAxisLayoutOffsetAtIndex(index),
        )
      ],
    );
  }

  ScrollController? getScrollController();

  AxisDirection getAxisDirection() => AxisDirection.down;

  int getChildCount() => 0;

  Widget? buildChild(BuildContext context, int index);

  double getChildMainAxisSizeAtIndex(int index);

  double getChildMainAxisLayoutOffsetAtIndex(int index);
}
