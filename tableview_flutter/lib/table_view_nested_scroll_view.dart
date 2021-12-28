import 'package:flutter/gestures.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_view_nested_scroll_controller.dart';

class TableViewNestedScrollView extends StatelessWidget {
  final TableViewNestedScrollController controller;
  final Axis scrollDirection;
  final bool reverse;
  final ScrollPhysics? physics;
  final NestedScrollViewHeaderSliversBuilder headerSliverBuilder;
  final Widget body;
  final DragStartBehavior dragStartBehavior;
  final bool floatHeaderSlivers;
  final Clip clipBehavior;
  final String? restorationId;
  final ScrollBehavior? scrollBehavior;

  TableViewNestedScrollView(
      {Key? key,
      required this.controller,
      this.scrollDirection = Axis.vertical,
      this.reverse = false,
      this.physics,
      required this.headerSliverBuilder,
      required this.body,
      this.dragStartBehavior = DragStartBehavior.start,
      this.floatHeaderSlivers = false,
      this.clipBehavior = Clip.hardEdge,
      this.restorationId,
      this.scrollBehavior});

  @override
  Widget build(BuildContext context) {
    return NotificationListener<ScrollNotification>(
      onNotification: (notification) {
        if(notification is ScrollEndNotification) {
          controller.onNestedViewScrollEnd();
          return true;
        } else if(notification is OverscrollNotification) {
          controller.onNestedViewOverScrolled(notification.overscroll);
          return true;
        } else return true;
      },
      child: Listener(
        onPointerMove: (event) {
          controller.onNestedViewPointerMove(event.delta.dy, event.delta.dx);
        },
        onPointerUp: (event) {
          controller.onNestedViewPointerUp();
        },
        onPointerCancel: (event) {
          controller.onNestedViewPointerCancel();
        },
        child: NestedScrollView(
          controller: controller.nestedScrollController,
          scrollDirection: scrollDirection,
          reverse: reverse,
          physics: physics,
          headerSliverBuilder: headerSliverBuilder,
          body: body,
          dragStartBehavior: dragStartBehavior,
          floatHeaderSlivers: floatHeaderSlivers,
          clipBehavior: clipBehavior,
          restorationId: restorationId,
          scrollBehavior: scrollBehavior,
        ),
      ),
    );
  }
}
