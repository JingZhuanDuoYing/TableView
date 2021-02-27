import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/header_row.dart';
import 'package:tableview_flutter/table_column_layout.dart';
import 'package:tableview_flutter/table_row.dart' as table_row;
import 'package:tableview_flutter/table_specs.dart';
import 'package:tableview_flutter/text_column.dart';

class TableView extends StatefulWidget {
  final specs = TableSpecs();

  @override
  State<StatefulWidget> createState() => _TableViewState();
}

class _TableViewState extends State<TableView> {
  HeaderRow headerRow = HeaderRow([]);

  @override
  void initState() {
    super.initState();
    _createData();
    if (widget.specs.stickyColumnsCount > 0) {
      for (var i = 0; i < widget.specs.stickyColumnsCount; i++) {
        widget.specs.viewColumnsWidthListener[i] = () {
          _onStickyColumnsWidthChanged();
        };
      }
    }
  }

  @override
  void dispose() {
    super.dispose();
    if (widget.specs.stickyColumnsCount > 0) {
      for (var i = 0; i < widget.specs.stickyColumnsCount; i++) {
        widget.specs.viewColumnsWidthListener[i] = null;
      }
    }
  }

  void _onStickyColumnsWidthChanged() {
    setState(() {});
  }

  void _createData() async {
    int columnsCount = 20;
    List<TextColumn> headerColumns = List.generate(columnsCount, (index) {
      return TextColumn("Title$index",
          alignment: Alignment.centerRight,
          leftMargin: 15,
          topMargin: 8,
          rightMargin: 8,
          bottomMargin: 8);
    });
    HeaderRow headerRow = HeaderRow(headerColumns);

    headerRow.stickyRows = List.generate(2, (index) {
      return table_row.TableRow(List.generate(columnsCount, (columnIndex) {
        return TextColumn("$index - $columnIndex",
            alignment: Alignment.centerRight,
            leftMargin: 15,
            topMargin: 8,
            rightMargin: 8,
            bottomMargin: 8);
      }));
    });

    headerRow.rows = List.generate(100, (index) {
      return table_row.TableRow(List.generate(columnsCount, (columnIndex) {
        return TextColumn("$index - $columnIndex",
            alignment: Alignment.centerRight,
            leftMargin: 15,
            topMargin: 8,
            rightMargin: 8,
            bottomMargin: 8);
      }));
    });

    widget.specs.init(headerRow, 2);

    headerRow.columns.forEach((column) {
      widget.specs.measureTextColumn(headerRow, column);
    });
    headerRow.stickyRows.forEach((row) {
      row.columns.forEach((column) {
        widget.specs.measureTextColumn(row, column);
      });
    });
    headerRow.rows.forEach((row) {
      row.columns.forEach((column) {
        widget.specs.measureTextColumn(row, column);
      });
    });

    setState(() {
      this.headerRow = headerRow;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (widget.specs.stickyColumnsCount <= 0) {
      return ListView.builder(
        scrollDirection: Axis.horizontal,
        itemCount: 20,
        itemBuilder: (context, index) {
          return TableColumnLayout(widget.specs, headerRow, index, false);
        },
      );
    }

    double stickyListViewWidth = 0;
    widget.specs.viewColumnsWidth
        .take(widget.specs.stickyColumnsCount)
        .forEach((element) {
      stickyListViewWidth += element;
    });
    return Row(
      children: [
        Container(
          width: stickyListViewWidth,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: widget.specs.stickyColumnsCount,
            itemBuilder: (context, index) {
              return TableColumnLayout(widget.specs, headerRow, index, true);
            },
          ),
        ),
        Expanded(
          child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: 20 - widget.specs.stickyColumnsCount,
              itemBuilder: (context, index) {
                return TableColumnLayout(widget.specs, headerRow,
                    index + widget.specs.stickyColumnsCount, false);
              }),
        )
      ],
    );
  }
}
