import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

typedef ChildMainAxisSizeProvider = double Function(int index);
typedef ChildMainAxisLayoutOffsetProvider = double Function(int index);

abstract class RecyclerView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scrollable(
      axisDirection: getAxisDirection(),
      controller: createScrollController(),
      viewportBuilder: (context, offset) => Viewport(
        axisDirection: getAxisDirection(),
        offset: offset,
        slivers: [
          _RecyclerList(
            SliverChildBuilderDelegate(
              (context, index) => buildChild(context, index),
              childCount: getChildCount(),
            ),
            (index) => getChildMainAxisSizeAtIndex(index),
            (index) => getChildMainAxisLayoutOffsetAtIndex(index),
          )
        ],
      ),
    );
  }

  AxisDirection getAxisDirection() => AxisDirection.down;

  ScrollController? createScrollController();

  int getChildCount() => 0;

  Widget? buildChild(BuildContext context, int index);

  double getChildMainAxisSizeAtIndex(int index);

  double getChildMainAxisLayoutOffsetAtIndex(int index);
}

class _RecyclerList extends SliverMultiBoxAdaptorWidget {
  final ChildMainAxisSizeProvider _sizeProvider;
  final ChildMainAxisLayoutOffsetProvider _offsetProvider;

  _RecyclerList(
      SliverChildDelegate delegate, this._sizeProvider, this._offsetProvider)
      : assert(delegate.estimatedChildCount != null),
        super(delegate: delegate);

  @override
  RenderSliverMultiBoxAdaptor createRenderObject(BuildContext context) {
    var element = context as SliverMultiBoxAdaptorElement;
    return _RecyclerViewAdapter(element, _sizeProvider, _offsetProvider);
  }
}

class _RecyclerViewAdapter extends RenderSliverFixedExtentBoxAdaptor {
  final ChildMainAxisSizeProvider _sizeProvider;
  final ChildMainAxisLayoutOffsetProvider _offsetProvider;

  _RecyclerViewAdapter(RenderSliverBoxChildManager childManager,
      this._sizeProvider, this._offsetProvider)
      : super(childManager: childManager);

  double _getMainAxisSizeByIndex(int index) {
    return _sizeProvider(index);
  }

  double _getMainAxisScrollOffsetByIndex(int index) {
    return _offsetProvider(index);
  }

  int _getChildIndexByMainAxisScrollOffset(double scrollOffset) {
    for (var i = 0; i < childManager.childCount; i++) {
      var childOffset = _getMainAxisScrollOffsetByIndex(i);
      var childSize = _getMainAxisSizeByIndex(i);
      if (childOffset <= scrollOffset && childOffset + childSize > scrollOffset)
        return i;
    }
    if (scrollOffset > _getMainAxisMaxScrollOffset())
      return childManager.childCount - 1;
    else
      return 0;
  }

  double _getMainAxisMaxScrollOffset() {
    var offset = .0;
    for (int i = 0; i < childManager.childCount; i++) {
      offset += _getMainAxisSizeByIndex(i);
    }
    return offset;
  }

  @override
  double indexToLayoutOffset(double itemExtent, int index) {
    return _getMainAxisScrollOffsetByIndex(index);
  }

  @override
  int getMinChildIndexForScrollOffset(double scrollOffset, double itemExtent) {
    return _getChildIndexByMainAxisScrollOffset(scrollOffset);
  }

  @override
  int getMaxChildIndexForScrollOffset(double scrollOffset, double itemExtent) {
    return _getChildIndexByMainAxisScrollOffset(scrollOffset);
  }

  @override
  double computeMaxScrollOffset(
      SliverConstraints constraints, double itemExtent) {
    return childManager.childCount * 100;
  }

  @override
  double get itemExtent => 0.0;

  @override
  void performLayout() {
    final SliverConstraints constraints = this.constraints;
    childManager.didStartLayout();
    childManager.setDidUnderflow(false);

    final double scrollOffset =
        constraints.scrollOffset + constraints.cacheOrigin;
    assert(scrollOffset >= 0.0);
    final double remainingExtent = constraints.remainingCacheExtent;
    assert(remainingExtent >= 0.0);
    final double targetEndScrollOffset = scrollOffset + remainingExtent;

    final int firstIndex = _getChildIndexByMainAxisScrollOffset(scrollOffset);
    final int? targetLastIndex = targetEndScrollOffset.isFinite
        ? _getChildIndexByMainAxisScrollOffset(targetEndScrollOffset)
        : null;

    if (firstChild != null) {
      final int leadingGarbage = _calculateLeadingGarbage(firstIndex);
      final int trailingGarbage = targetLastIndex != null
          ? _calculateTrailingGarbage(targetLastIndex)
          : 0;
      collectGarbage(leadingGarbage, trailingGarbage);
    } else {
      collectGarbage(0, 0);
    }

    if (firstChild == null) {
      if (!addInitialChild(
          index: firstIndex,
          layoutOffset: _getMainAxisScrollOffsetByIndex(firstIndex))) {
        // There are either no children, or we are past the end of all our children.
        final double max;
        if (firstIndex <= 0) {
          max = 0.0;
        } else {
          max = _getMainAxisMaxScrollOffset();
        }
        geometry = SliverGeometry(
          scrollExtent: max,
          maxPaintExtent: max,
        );
        childManager.didFinishLayout();
        return;
      }
    }

    RenderBox? trailingChildWithLayout;

    for (int index = indexOf(firstChild!) - 1; index >= firstIndex; --index) {
      final RenderBox? child =
          insertAndLayoutLeadingChild(constraints.asBoxConstraints());
      if (child == null) {
        // Items before the previously first child are no longer present.
        // Reset the scroll offset to offset all items prior and up to the
        // missing item. Let parent re-layout everything.
        geometry = SliverGeometry(
            scrollOffsetCorrection: _getMainAxisScrollOffsetByIndex(index));
        return;
      }
      final SliverMultiBoxAdaptorParentData childParentData =
          child.parentData! as SliverMultiBoxAdaptorParentData;
      childParentData.layoutOffset = _getMainAxisScrollOffsetByIndex(index);
      assert(childParentData.index == index);
      trailingChildWithLayout ??= child;
    }

    if (trailingChildWithLayout == null) {
      firstChild!.layout(constraints.asBoxConstraints());
      final SliverMultiBoxAdaptorParentData childParentData =
          firstChild!.parentData! as SliverMultiBoxAdaptorParentData;
      childParentData.layoutOffset =
          _getMainAxisScrollOffsetByIndex(firstIndex);
      trailingChildWithLayout = firstChild;
    }

    double estimatedMaxScrollOffset = _getMainAxisMaxScrollOffset();
    for (int index = indexOf(trailingChildWithLayout!) + 1;
        targetLastIndex == null || index <= targetLastIndex;
        ++index) {
      RenderBox? child = childAfter(trailingChildWithLayout!);
      if (child == null || indexOf(child) != index) {
        child = insertAndLayoutChild(constraints.asBoxConstraints(),
            after: trailingChildWithLayout);
        if (child == null) {
          // We have run out of children.
          // estimatedMaxScrollOffset = _getMainAxisMaxScrollOffset();
          break;
        }
      } else {
        child.layout(constraints.asBoxConstraints());
      }
      trailingChildWithLayout = child;
      assert(child != null);
      final SliverMultiBoxAdaptorParentData childParentData =
          child.parentData! as SliverMultiBoxAdaptorParentData;
      assert(childParentData.index == index);
      childParentData.layoutOffset =
          _getMainAxisScrollOffsetByIndex(childParentData.index!);
    }

    final int lastIndex = indexOf(lastChild!);
    final double leadingScrollOffset =
        _getMainAxisScrollOffsetByIndex(firstIndex);
    final double trailingScrollOffset =
        _getMainAxisScrollOffsetByIndex(lastIndex + 1);

    assert(firstIndex == 0 ||
        childScrollOffset(firstChild!)! - scrollOffset <=
            precisionErrorTolerance);
    assert(debugAssertChildListIsNonEmptyAndContiguous());
    assert(indexOf(firstChild!) == firstIndex);
    assert(targetLastIndex == null || lastIndex <= targetLastIndex);

    final double paintExtent = calculatePaintOffset(
      constraints,
      from: leadingScrollOffset,
      to: trailingScrollOffset,
    );

    final double cacheExtent = calculateCacheOffset(
      constraints,
      from: leadingScrollOffset,
      to: trailingScrollOffset,
    );

    final double targetEndScrollOffsetForPaint =
        constraints.scrollOffset + constraints.remainingPaintExtent;
    final int? targetLastIndexForPaint = targetEndScrollOffsetForPaint.isFinite
        ? getMaxChildIndexForScrollOffset(targetEndScrollOffsetForPaint, .0)
        : null;
    geometry = SliverGeometry(
      scrollExtent: estimatedMaxScrollOffset,
      paintExtent: paintExtent,
      cacheExtent: cacheExtent,
      maxPaintExtent: estimatedMaxScrollOffset,
      // Conservative to avoid flickering away the clip during scroll.
      hasVisualOverflow: (targetLastIndexForPaint != null &&
              lastIndex >= targetLastIndexForPaint) ||
          constraints.scrollOffset > 0.0,
    );

    // We may have started the layout while scrolled to the end, which would not
    // expose a new child.
    if (estimatedMaxScrollOffset == trailingScrollOffset)
      childManager.setDidUnderflow(true);
    childManager.didFinishLayout();
  }

  int _calculateLeadingGarbage(int firstIndex) {
    RenderBox? walker = firstChild;
    int leadingGarbage = 0;
    while (walker != null && indexOf(walker) < firstIndex) {
      leadingGarbage += 1;
      walker = childAfter(walker);
    }
    return leadingGarbage;
  }

  int _calculateTrailingGarbage(int targetLastIndex) {
    RenderBox? walker = lastChild;
    int trailingGarbage = 0;
    while (walker != null && indexOf(walker) > targetLastIndex) {
      trailingGarbage += 1;
      walker = childBefore(walker);
    }
    return trailingGarbage;
  }
}
