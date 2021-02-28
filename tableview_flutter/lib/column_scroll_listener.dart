import 'package:flutter/gestures.dart';
import 'package:flutter/widgets.dart';
import 'package:tableview_flutter/table_specs.dart';

class ColumnScrollListener extends NotificationListener<ScrollNotification> {
  final ScrollController controller;
  final TableSpecs specs;

  ColumnScrollListener(this.specs, this.controller, Widget child)
      : super(
            onNotification: (ScrollNotification notification) {
              if (notification is ScrollStartNotification) {
                if (null == specs.scrollingController) {
                  specs.scrollingController = controller;
                } else if (notification?.dragDetails?.kind ==
                    PointerDeviceKind.touch) {
                  if (specs.scrollingController?.hasClients == true) {
                    specs.scrollingController?.jumpTo(specs.offset);
                  }
                  specs.scrollingController = controller;
                }
              } else if (notification is ScrollEndNotification) {
                if (controller == specs.scrollingController) {
                  specs.scrollingController = null;
                }
              } else if (notification is ScrollUpdateNotification) {
                if (null != specs.scrollingController) {
                  specs.onScrolled();
                }
              }
              return true;
            },
            child: child);
}
