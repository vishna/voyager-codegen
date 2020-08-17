/// Generated file, DO NOT EDIT
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:meta/meta.dart';
import 'package:voyager/voyager.dart' hide Router;
import 'package:voyager/voyager.dart' as voyager;

typedef WidgetWrapper<T extends VoyagerTestScenarios> = Widget Function(
    Widget nonWrappedWidget, voyager.Router router, T scenarios);

@isTest
void _testVoyagerWidget<T extends VoyagerTestScenarios>(
  String description,
  Future<voyager.Router> routerFuture,
  WidgetWrapper widgetWrapper,
  T scenarios,
  VoyagerTestScenario scenario, {
  bool skip = false,
  Timeout timeout,
  Duration initialTimeout,
  bool semanticsEnabled = false,
}) {
  testWidgets(description, (WidgetTester tester) async {
    voyager.Router router;
    await tester.runAsync(() async {
      router = await routerFuture;
      expect(router, isNotNull);
    });

    VoyagerArgument argument;

    if (scenario.argument != null) {
      if (scenario.argument is VoyagerArgument) {
        argument = scenario.argument;
      } else {
        argument = VoyagerArgument(scenario.argument);
      }
    }

    var widget = scenario.stateless == false
        ? VoyagerWidget(
            path: scenario.path(), router: router, argument: argument)
        : VoyagerStatelessWidget(
            path: scenario.path(),
            router: router,
            argument: argument,
            useCache: true);
    widget = widgetWrapper != null
        ? widgetWrapper(widget, router, scenarios)
        : widget;
    widget = scenario.widgetWrapper != null
        ? scenario.widgetWrapper(widget, router, scenarios)
        : widget;

    await tester.pumpWidget(widget);
    await scenario.widgetTesterCallback(tester);
  },
      skip: skip,
      timeout: timeout,
      initialTimeout: initialTimeout,
      semanticsEnabled: semanticsEnabled);
}

@experimental
abstract class VoyagerTestScenario {
  VoyagerTestScenario(this.testDescription, this.widgetTesterCallback,
      {this.argument, this.stateless = false});

  final String testDescription;
  final WidgetTesterCallback widgetTesterCallback;
  final dynamic argument;
  final bool stateless;
  WidgetWrapper widgetWrapper;

  String path();
  void addWidgetWrapper(WidgetWrapper widgetWrapper) {
    this.widgetWrapper = widgetWrapper;
  }
}

@experimental
class VoyagerTestHomeScenario extends VoyagerTestScenario {
  VoyagerTestHomeScenario.write(WidgetTesterCallback widgetTesterCallback,
      {String description = "", dynamic argument, bool stateless = false})
      : super(description, widgetTesterCallback,
            argument: argument, stateless: stateless);

  @override
  String path() {
    return "/home";
  }
}

@experimental
class VoyagerTestOtherScenario extends VoyagerTestScenario {
  VoyagerTestOtherScenario.write(
      this.title, WidgetTesterCallback widgetTesterCallback,
      {String description = "", dynamic argument, bool stateless = false})
      : super(description, widgetTesterCallback,
            argument: argument, stateless: stateless);

  final String title;

  @override
  String path() {
    return "/other/$title";
  }
}

@experimental
class VoyagerTestFabScenario extends VoyagerTestScenario {
  VoyagerTestFabScenario.write(WidgetTesterCallback widgetTesterCallback,
      {String description = "", dynamic argument, bool stateless = false})
      : super(description, widgetTesterCallback,
            argument: argument, stateless: stateless);

  @override
  String path() {
    return "/fab";
  }
}

@experimental
abstract class VoyagerTestScenarios {
  const VoyagerTestScenarios(this.defaultWrapper);
  final WidgetWrapper defaultWrapper;

  List<VoyagerTestHomeScenario> homeScenarios();
  List<VoyagerTestOtherScenario> otherScenarios();
  List<VoyagerTestFabScenario> fabScenarios();
}

@isTestGroup
@experimental
void voyagerAutomatedTests<T extends VoyagerTestScenarios>(String description,
    Future<voyager.Router> router, VoyagerTestScenarios testScenarios,
    {bool forceTests = true}) {
  group(description, () {
    final homeScenarios = testScenarios.homeScenarios();
    if (forceTests) {
      assert(
          homeScenarios != null, "homeScenarios seems to lack implementation");
      assert(homeScenarios.isNotEmpty,
          "homeScenarios must return at least one test scenario");
    }
    homeScenarios?.asMap()?.forEach((index, scenario) {
      _testVoyagerWidget<T>(
          "home scenario $index: path=${scenario.path()} ${scenario.testDescription}",
          router,
          testScenarios.defaultWrapper,
          testScenarios,
          scenario);
    });
    final otherScenarios = testScenarios.otherScenarios();
    if (forceTests) {
      assert(otherScenarios != null,
          "otherScenarios seems to lack implementation");
      assert(otherScenarios.isNotEmpty,
          "otherScenarios must return at least one test scenario");
    }
    otherScenarios?.asMap()?.forEach((index, scenario) {
      _testVoyagerWidget<T>(
          "other scenario $index: path=${scenario.path()} ${scenario.testDescription}",
          router,
          testScenarios.defaultWrapper,
          testScenarios,
          scenario);
    });
    final fabScenarios = testScenarios.fabScenarios();
    if (forceTests) {
      assert(fabScenarios != null, "fabScenarios seems to lack implementation");
      assert(fabScenarios.isNotEmpty,
          "fabScenarios must return at least one test scenario");
    }
    fabScenarios?.asMap()?.forEach((index, scenario) {
      _testVoyagerWidget<T>(
          "fab scenario $index: path=${scenario.path()} ${scenario.testDescription}",
          router,
          testScenarios.defaultWrapper,
          testScenarios,
          scenario);
    });
  });
}
