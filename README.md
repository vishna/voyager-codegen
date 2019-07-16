# voyager-codegen

![](https://media.giphy.com/media/w3J7mstYCISqs/giphy.gif)

> Kot is a cat in Polish and voyager is in space, therefore the above gif.

## Overview

voyager-codegen is a complimentary tool for the flutter library [voyager](https://github.com/vishna/voyager)... written in Kotlin.

## Usage

Firstly dowload `voyager-codegen.jar` from the internets to where your flutter project is (that is where `pubspec.yaml` is):

```
wget TODO
```

Next up start voyager-codegen:

```
java -jar voyager-codegen.jar
```

The above will bootsrtap `voyager-cogegen.yaml` in your flutter project, like so:

```yaml
- name: VoyagerPaths
  source: # TODO supply location of your voyager map
  target: lib/gen/voyager_paths.dart
```

Obviously you need to edit this file and point it to location where voyager's configuration yaml is stored. You can point it directly to a yaml file or a dart file - it will try to find first triple ```'''``` quoted string in that file - be warned 😱 Sorry - if you go for yaml in source code just don't keep it with anything else.

`voyager-codegen` will keep on watching the project file and generate dart files if applicable.