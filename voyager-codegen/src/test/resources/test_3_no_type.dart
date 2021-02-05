/// Generated file, DO NOT EDIT
// ignore_for_file: public_member_api_docs
part of 'test_3.dart';

String pathObjectClass(String class_) {
  return "/_object/$class_";
}

const String typeObjectClass = "_object_class";
const String pathFab = "/fab";
const String typeFab = "fab";
const String pathHome = "/home";
const String typeHome = "home";
String pathKeywordsAwaitAsync({String await_, String async_}) {
  return "/keywords/$await_/$async_";
}

const String typeKeywordsAwaitAsync = "keywords_await_async";
const String pathList = "/list";
const String typeList = "list";
String pathOtherTitle(String title) {
  return "/other/$title";
}

const String typeOtherTitle = "other_title";

extension VoyagerData on Voyager {
  String? get title => this["title"];
  String? get body => this["body"];
  String? get fabPath => this["fabPath"];
  String? get target => this["target"];
  bool? get await_ => this["await"];
  Icon? get icon => this["icon"];
  List<dynamic>? get actions => this["actions"];
  List<dynamic>? get items => this["items"];
}

abstract class IconPluginStub extends VoyagerObjectPlugin<Icon> {
  IconPluginStub() : super("icon");
}
