/// Generated file, DO NOT EDIT
// ignore_for_file: public_member_api_docs
part of 'test_3.dart';

const String pathHome = "/home";
const String typeHome = "home";
String pathOther(String title) {
  return "/other/$title";
}

const String typeOther = "other";
const String pathFab = "/fab";
const String typeFab = "fab";
const String pathList = "/list";
const String typeList = "list";
String pathObjectItem(String class_) {
  return "/_object/$class_";
}

const String typeObjectItem = "object_item";
String pathSuper({String await_, String async_}) {
  return "/keywords/$await_/$async_";
}

const String typeSuper = "super";

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
