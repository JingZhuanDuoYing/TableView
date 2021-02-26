import 'dart:collection';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

void main() {
  runApp(MyApp());
}

class Holder {
  final List<ScrollController> controllers = List.filled(15, null);
  final List<double> columnsWidth = List.filled(15, 110);
  final List<VoidCallback> columnsWidthListeners = List.filled(15, null);
  ScrollController scrollingController;
  var offset = 0.0;

  void _onScrolled() {
    var controller = scrollingController;
    if(controller?.hasClients != true) return;
    offset = controller.offset;
    controllers.forEach((element) {
      if (element == controller) return;
      if (element?.hasClients != true) return;
      if (element.offset == offset) return;
      element.jumpTo(offset);
    });
  }

  void _onColumnsWidthChanged(int columnIndex, double width) {
    columnsWidth[columnIndex] = width;
    columnsWidthListeners[columnIndex]?.call();
  }

  TextPainter _calculateText(BuildContext context, String value,
      double fontSize, FontWeight fontWeight, double maxWidth, int maxLines) {
    TextPainter painter = TextPainter(
        maxLines: maxLines,
        textDirection: TextDirection.ltr,
        text: TextSpan(
            text: value,
            style: TextStyle(
              fontWeight: fontWeight,
              fontSize: fontSize,
            )));
    painter.layout(maxWidth: maxWidth);

    ///文字的宽度:painter.width
    return painter;
  }
}

class MyApp extends StatelessWidget {
  final holder = Holder();

  @override
  Widget build(BuildContext context) {
    final title = 'Long List';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: Container(
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: 15,
            itemBuilder: (context, index) {
              return ColumnList(holder, index);
            },
          ),
        ),
      ),
    );
  }
}

class ColumnList extends StatefulWidget {
  final Holder holder;
  final int columnIndex;

  ColumnList(this.holder, this.columnIndex);

  @override
  State<StatefulWidget> createState() => _ColumnListState();
}

class _ColumnListState extends State<ColumnList> {
  @override
  void initState() {
    super.initState();
    widget.holder.columnsWidthListeners[widget.columnIndex] = () {
      setState(() {});
    };
  }

  @override
  Widget build(BuildContext context) {
    widget.holder.controllers[widget.columnIndex]?.dispose();
    var controller =
        ScrollController(initialScrollOffset: widget.holder.offset);
    widget.holder.controllers[widget.columnIndex] = controller;
    return Container(
      width: widget.holder.columnsWidth[widget.columnIndex],
      child: NotificationListener<ScrollNotification>(
        onNotification: (ScrollNotification notification) {
          if (notification is ScrollStartNotification) {
            if (null == widget.holder.scrollingController) {
              widget.holder.scrollingController = controller;
            } else if (notification?.dragDetails?.kind ==
                PointerDeviceKind.touch) {
              if (widget.holder.scrollingController?.hasClients == true) {
                widget.holder.scrollingController?.jumpTo(widget.holder.offset);
              }
              widget.holder.scrollingController = controller;
            }
          } else if (notification is ScrollEndNotification) {
            if (controller == widget.holder.scrollingController) {
              widget.holder.scrollingController = null;
            }
          } else if (notification is ScrollUpdateNotification) {
            if (null != widget.holder.scrollingController) {
              widget.holder._onScrolled();
            }
          }
          return true;
        },
        child: ListView.builder(
          shrinkWrap: true,
          controller: controller,
          itemCount: 100,
          itemBuilder: (context, rowIndex) {
            return TextColumn(widget.columnIndex, rowIndex);
          },
        ),
      ),
    );
  }
}

class TextColumn extends StatelessWidget {
  final int rowIndex;
  final int columnIndex;

  TextColumn(this.columnIndex, this.rowIndex);

  @override
  Widget build(BuildContext context) {
    var text = rowIndex == 99
        ? '||||||||||||||||||Row: $rowIndex, column: $columnIndex'
        : 'Row: $rowIndex, column: $columnIndex';
    var parent = context.findAncestorWidgetOfExactType<ColumnList>();
    var painter = parent.holder._calculateText(
        context, text, 14, FontWeight.normal, double.infinity, 1);
    var expectWidth = painter.width + 25;
    if (expectWidth > parent.holder.columnsWidth[columnIndex]) {
      WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
        if (expectWidth > parent.holder.columnsWidth[columnIndex]) {
          parent.holder._onColumnsWidthChanged(columnIndex, expectWidth);
        }
      });
    }
    return Container(
      padding: EdgeInsets.fromLTRB(15, 8, 10, 8),
      child: Text(
        text,
        maxLines: 1,
      ),
    );
  }
}
