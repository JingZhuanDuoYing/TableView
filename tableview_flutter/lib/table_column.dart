import 'package:flutter/painting.dart';
import 'package:flutter/widgets.dart';
import 'package:meta/meta.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';

abstract class TableColumn {
  double? width;
  double? height;
  double minWidth = 0;
  double minHeight = 0;
  double maxWidth = double.infinity;
  double maxHeight = double.infinity;

  double leftMargin = 0;
  double topMargin = 0;
  double rightMargin = 0;
  double bottomMargin = 0;

  double paddingLeft = 0;
  double paddingTop = 0;
  double paddingRight = 0;
  double paddingBottom = 0;

  AlignmentGeometry? alignment;

  Color? backgroundColor;

  bool visible = true;

  @internal
  double columnWidth = 0;
  @internal
  double columnHeight = 0;

  TableColumn(
      {double? width,
      double? height,
      double? minWidth,
      double? minHeight,
      double? maxWidth,
      double? maxHeight,
      double? leftMargin,
      double? topMargin,
      double? rightMargin,
      double? bottomMargin,
      double? paddingLeft,
      double? paddingTop,
      double? paddingRight,
      double? paddingBottom,
      AlignmentGeometry? alignment,
      Color? backgroundColor,
      bool? visible}) {
    this.width = width ?? this.width;
    this.height = height ?? this.height;
    this.minWidth = minWidth ?? this.minWidth;
    this.minHeight = minHeight ?? this.minHeight;
    this.maxWidth = maxWidth ?? this.maxWidth;
    this.maxHeight = maxHeight ?? this.maxHeight;
    this.leftMargin = leftMargin ?? this.leftMargin;
    this.topMargin = topMargin ?? this.topMargin;
    this.rightMargin = rightMargin ?? this.rightMargin;
    this.bottomMargin = bottomMargin ?? this.bottomMargin;
    this.paddingLeft = paddingLeft ?? this.paddingLeft;
    this.paddingTop = paddingTop ?? this.paddingTop;
    this.paddingRight = paddingRight ?? this.paddingRight;
    this.paddingBottom = paddingBottom ?? this.paddingBottom;
    this.alignment = alignment ?? this.alignment;
    this.backgroundColor = backgroundColor ?? this.backgroundColor;
    this.visible = visible ?? this.visible;
  }

  Widget build(BuildContext context, TableSpecs specs, table_row.TableRow row,
      int index);

  void setMinSize() {
    double emptyWidth = addEssentialSpace(0, true);
    columnWidth = emptyWidth.clamp(minWidth, maxWidth);
    double emptyHeight = addEssentialSpace(0, false);
    columnHeight = emptyHeight.clamp(minHeight, maxHeight);
  }

  double addEssentialSpace(double size, bool horizontal) {
    if (horizontal) {
      return size + leftMargin + paddingLeft + paddingRight + rightMargin;
    } else {
      return size + topMargin + paddingTop + paddingBottom + bottomMargin;
    }
  }
}
