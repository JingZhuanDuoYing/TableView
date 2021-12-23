import 'package:flutter/cupertino.dart';

typedef TableViewStoppableScrollPhysicsCallback = bool Function();

class TableViewStoppableScrollPhysics extends ScrollPhysics {
  final TableViewStoppableScrollPhysicsCallback? callback;

  TableViewStoppableScrollPhysics(this.callback, {ScrollPhysics? parent})
      : super(parent: parent);

  @override
  ScrollPhysics applyTo(ScrollPhysics? ancestor) {
    return TableViewStoppableScrollPhysics(callback, parent: buildParent(ancestor));
  }

  @override
  bool shouldAcceptUserOffset(ScrollMetrics position) {
    var stopScroll = callback?.call() ?? false;
    return !stopScroll;
  }

  @override
  bool get allowImplicitScrolling {
    var stopScroll = callback?.call() ?? false;
    return !stopScroll;
  }
}
