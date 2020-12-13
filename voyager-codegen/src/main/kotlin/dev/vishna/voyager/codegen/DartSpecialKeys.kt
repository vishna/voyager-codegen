package dev.vishna.voyager.codegen

/**
 * Sanitizes special Dart keys e.g.
 * class -> class_
 */
fun String.dartSanitize() =
    if (this in specialKeywordsList) {
        "${this}_"
    } else {
        this
    }

/**
 * Voyager types are nullable by nature but you can force them to be non-nullable
 * by explicitly adding !
 */
fun String.dartNullableType() =
        if (endsWith("!")){
            removeSuffix("!")
        } else {
            "$this?"
        }

private val specialKeywordsList by lazy {
    specialKeywords.split("\n").map { it.trim() }.filter { !it.isNullOrBlank() }
}

// list via: https://dart.dev/guides/language/language-tour#keywords
private val specialKeywords = """
abstract
dynamic
implements
show
as
else
import
static
assert
enum
super
async
export
interface
switch
await
extends
sync
break
external
library
this
case
factory
mixin
throw
catch
false
new
true
class
final
null
try
const
finally
typedef
continue
for
operator
var
covariant
Function
part
void
default
get
rethrow
while
deferred
hide
return
with
do
if
set
yield
"""