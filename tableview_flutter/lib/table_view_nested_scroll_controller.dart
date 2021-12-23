import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_view_stoppable_scroll_physics.dart';

class TableViewNestedScrollController {
  final ScrollController nestedScrollController;
  bool stopScroll = true;

  TableViewNestedScrollController(this.nestedScrollController);

  late TableViewStoppableScrollPhysics nestedScrollPhysic =
      TableViewStoppableScrollPhysics(() => stopScroll);

  void _setStopScroll(bool stopScroll) {
    var stateChanged = this.stopScroll != stopScroll;
    this.stopScroll = stopScroll;
    if (stateChanged && stopScroll) {
      var position = nestedScrollController.positions.isNotEmpty == true
          ? nestedScrollController.positions.first
          : null;
      if (null != position &&
          position.maxScrollExtent > 0 &&
          position.pixels == position.maxScrollExtent) {
        position.jumpTo(position.maxScrollExtent - 1);
      }
    }
  }

  void onTableViewScrollEnd(ScrollController controller) {
    if (controller.hasClients &&
        controller.position.pixels == 0.0 &&
        !stopScroll) {
      _setStopScroll(true);
    }
  }

  void onNestedViewScrollEnd() {
    var position = nestedScrollController.positions.isNotEmpty == true
        ? nestedScrollController.positions.first
        : null;
    _setStopScroll(null != position &&
        position.maxScrollExtent.toInt() != position.pixels.toInt());
  }

  void onPointerMove(ScrollController controller, double delta) {
    if (!stopScroll && controller.hasClients) {
      var position = controller.positions.isNotEmpty == true
          ? controller.positions.first
          : null;
      if (null != position && position.pixels == 0.0 && delta > 0.0) {
        _setStopScroll(true);
      }
    }
  }
}
