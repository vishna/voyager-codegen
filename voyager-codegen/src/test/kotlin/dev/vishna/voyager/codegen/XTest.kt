package dev.vishna.voyager.codegen

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class XTest {

    @Test
    fun typifyTest() {
        "/home".typify() `should be equal to` "home"
        "/other/:title".typify() `should be equal to` "other_title"
        "/_object/:class".typify() `should be equal to` "_object_class"
        "/keywords/:await/:async".typify() `should be equal to` "keywords_await_async"
    }

    @Test
    fun dartKeySanitization() {
        "const".dartSanitize() shouldBeEqualTo "const_"
        "Const".dartSanitize() shouldBeEqualTo "Const"
        "foo".dartSanitize() shouldBeEqualTo "foo"
        "class".dartSanitize() shouldBeEqualTo "class_"
        "Class".dartSanitize() shouldBeEqualTo "Class"
    }
}