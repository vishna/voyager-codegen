# voyager-codegen

![](http://giphygifs.s3.amazonaws.com/media/TF3uA6xGKOohO/giphy.gif)

## Overview

voyager-codegen is a complimentary tool for the flutter library [voyager](https://github.com/vishna/voyager) written in Kotlin using [patrol](https://github.com/vishna/patrol).

## Usage

Download ready to use `voyager-codegen.jar` from jitpack and put it where your flutter project is (that is where `pubspec.yaml` is):

```
wget https://jitpack.io/com/github/vishna/voyager-codegen/cli/master-SNAPSHOT/cli-master-SNAPSHOT-all.jar -O voyager-codegen.jar
```

Next up start voyager-codegen:

```
java -jar voyager-codegen.jar
```

The above will bootstrap `voyager-cogegen.yaml` in your flutter project, like so:

```yaml
- name: Voyager # base name for generated classes, e.g. VoyagerPaths, VoyagerTests etc.
  source: # TODO supply location of your voyager map
  target: lib/gen/voyager_gen.dart
```

Obviously you need to edit this file and point it to location where voyager's configuration yaml is stored. You can point it directly to a yaml file or a dart file - it will try to find first triple ```'''``` quoted string in that file - be warned 😱 Sorry - if you go for yaml in source code just don't keep it with anything else.

`voyager-codegen` will keep on watching the project file and generate dart files if applicable.


## More options
```
Usage: voyager-codegen [OPTIONS]

  Code generation utility for the Voyager project.

Options:
  --run-once  Runs voyager-codegen only once, doesn't watch file system,
              useful for CI/CD.
  --dry-run   Runs voyager-codegen in a dry mode
  --debug     Runs voyager-codegen in a debug mode
  -h, --help  Show this message and exit
```