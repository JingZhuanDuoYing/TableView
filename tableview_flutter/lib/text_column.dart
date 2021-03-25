import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_column.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';

class TextColumn extends TableColumn {
  TextStyle textStyle = TextStyle();
  String? text;

  TextColumn(String? text,
      {TextStyle? textStyle,
      double? width,
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
      bool? visible})
      : super(
            width: width,
            height: height,
            minWidth: minWidth,
            minHeight: minHeight,
            maxWidth: maxWidth,
            maxHeight: maxHeight,
            leftMargin: leftMargin,
            topMargin: topMargin,
            rightMargin: rightMargin,
            bottomMargin: bottomMargin,
            paddingLeft: paddingLeft,
            paddingTop: paddingTop,
            paddingRight: paddingRight,
            paddingBottom: paddingBottom,
            alignment: alignment,
            backgroundColor: backgroundColor,
            visible: visible) {
    this.textStyle = textStyle ?? this.textStyle;
    this.text = text;
  }

  void setTextSize(double textWidth, double textHeight) {
    if (null != width) {
      columnWidth = width!;
    } else {
      double expectWidth = addEssentialSpace(textWidth, true);
      columnWidth = expectWidth.clamp(minWidth, maxWidth);
    }
    if (null != height) {
      columnHeight = height!;
    } else {
      double expectHeight = addEssentialSpace(textHeight, false);
      columnHeight = expectHeight.clamp(minHeight, maxHeight);
    }
  }

  @override
  Widget build(BuildContext context, TableSpecs specs, table_row.TableRow row,
      int index) {
    var containerWidth =
        specs.viewColumnsWidth[index] - leftMargin - rightMargin;
    var containerHeight = row.rowHeight - topMargin - bottomMargin;

    return Container(
      color: backgroundColor,
      alignment: alignment,
      margin:
          EdgeInsets.fromLTRB(leftMargin, topMargin, rightMargin, bottomMargin),
      padding: EdgeInsets.fromLTRB(
          paddingLeft, paddingTop, paddingRight, paddingBottom),
      width: containerWidth,
      height: containerHeight,
      child: Text(
        text ?? '',
        style: textStyle,
      ),
    );
  }
}
