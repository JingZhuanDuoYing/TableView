import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_view_stoppable_scroll_physics.dart';

class TableViewNestedScrollController {
  final ScrollController nestedScrollController;
  final ScrollController tableViewScrollController = ScrollController();
  bool tableViewStopScroll = true;
  bool nestedViewStopScroll = false;
  var handlePointerMove = false;

  TableViewNestedScrollController(this.nestedScrollController);

  late TableViewStoppableScrollPhysics tableViewScrollPhysics =
      TableViewStoppableScrollPhysics(() => tableViewStopScroll);

  void _setStopScroll(bool stopScroll) {
    var stateChanged = this.tableViewStopScroll != stopScroll;
    this.tableViewStopScroll = stopScroll;
    if (!stateChanged) return;
    if (stopScroll) {
      var nestedPosition = nestedScrollController.positions.isNotEmpty == true
          ? nestedScrollController.positions.first
          : null;
      if (null != nestedPosition &&
          nestedPosition.maxScrollExtent > 0 &&
          nestedPosition.pixels == nestedPosition.maxScrollExtent) {
        nestedPosition.jumpTo(nestedPosition.maxScrollExtent - 0.1);
      }
    }
  }

  void onTableViewScrollEnd(ScrollController controller) {
    if (controller.hasClients &&
        controller.position.pixels == 0.0 &&
        !tableViewStopScroll) {
      _setStopScroll(true);
    }
  }

  void onNestedViewScrollEnd() {
    var position = nestedScrollController.positions.isNotEmpty == true
        ? nestedScrollController.positions.first
        : null;
    var stopScroll = null != position &&
        position.maxScrollExtent.toInt() != position.pixels.toInt();
    _setStopScroll(stopScroll);
  }

  void onNestedViewOverScrolled(double overscroll) {
    if(!handlePointerMove) return;
    var position = tableViewScrollController.positions.isNotEmpty == true
        ? tableViewScrollController.positions.first
        : null;
    if (null == position) return;
    var newPosition = position.pixels + overscroll;
    position.jumpTo(newPosition);
  }

  void onNestedViewPointerMove(double verticalDelta, double horizontalDelta) {
    if (!tableViewScrollController.hasClients) return;
    var position = tableViewScrollController.positions.isNotEmpty == true
        ? tableViewScrollController.positions.first
        : null;
    if (null == position) return;
    if (tableViewStopScroll) {
      if (position.pixels == 0.0 &&
          verticalDelta < 0.0 &&
          horizontalDelta.abs() < verticalDelta.abs()) {
        var nestedPosition = nestedScrollController.positions.isNotEmpty == true
            ? nestedScrollController.positions.first
            : null;
        if (null != nestedPosition &&
            nestedPosition.maxScrollExtent > 0 &&
            nestedPosition.pixels == nestedPosition.maxScrollExtent - 0.1) {
          handlePointerMove = true;
          nestedPosition.jumpTo(nestedPosition.maxScrollExtent);
          var newPosition = position.pixels - verticalDelta;
          position.jumpTo(newPosition);
          _setStopScroll(false);
        }
      } else if (position.pixels == 0.0 &&
          horizontalDelta.abs() > verticalDelta.abs()) {
        var nestedPosition = nestedScrollController.positions.isNotEmpty == true
            ? nestedScrollController.positions.first
            : null;
        if (null != nestedPosition &&
            nestedPosition.maxScrollExtent > 0 &&
            nestedPosition.pixels == nestedPosition.maxScrollExtent - 0.1) {
          nestedPosition.jumpTo(nestedPosition.maxScrollExtent);
          _setStopScroll(false);
        }
      }
    } else {
      if (handlePointerMove) {
        var newPosition = position.pixels - verticalDelta;
        position.jumpTo(newPosition);
      } else {
        if (position.pixels == 0.0 &&
            verticalDelta > 0.0 &&
            horizontalDelta.abs() < verticalDelta.abs()) {
          _setStopScroll(true);
        }
      }
    }
  }

  void onNestedViewPointerUp() {
    handlePointerMove = false;
  }

  void onNestedViewPointerCancel() {
    handlePointerMove = false;
  }
}
