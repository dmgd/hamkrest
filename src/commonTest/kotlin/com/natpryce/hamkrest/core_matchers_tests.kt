package com.natpryce.hamkrest

import com.natpryce.hamkrest.assertion.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AnythingAndNothing {
    @Test
    fun any_value_is_anything() {
        assertMatch(anything("a string"))
        assertMatch(anything(99))
        assertMatch(anything(99.0))
        assertMatch(anything(true))
        assertMatch(anything(false))
        assertMatch(anything(object {}))
    }
    
    @Test
    fun null_is_anything() {
        assertMatch(anything(null))
    }
    
    @Test
    fun any_value_is_not_nothing() {
        assertMismatch(nothing("a string"))
        assertMismatch(nothing(99))
        assertMismatch(nothing(99.0))
        assertMismatch(nothing(true))
        assertMismatch(nothing(false))
        assertMismatch(nothing(object {}))
    }
    
    @Test
    fun null_is_not_nothing() {
        assertMismatch(nothing(null))
    }
}

class Equality {
    @Test
    fun equal() {
        assertMatch((equalTo(10))(10))
        assertMatch(equalTo("hello")("hello"))
    }

    @Test
    fun not_equal() {
        assertMismatchWithDescription("was: 20", equalTo(10)(20))
        assertMismatchWithDescription("was: 1", equalTo(0)(1))
    }
    
    @Test
    fun null_comparison() {
        assertMatch((equalTo(null)(null)))
        
        val actual: String? = null
        assertMatch(equalTo<String?>(null)(actual))
        
        val expected: String? = null
        assertMatch(equalTo(expected)(null))
        
        assertMismatchWithDescription("was: null", equalTo("foo")(actual))
    }

    @Test
    fun description() {
        assertEquals("is equal to 20", equalTo(20).description)
        assertEquals("is equal to \"foo\"", equalTo("foo").description)
        assertEquals("is equal to \"hello \\\"nat\\\"\"", equalTo("hello \"nat\"").description)
        assertEquals("is equal to null", equalTo(null).description)
    }
}

class SameInstance {
    class Example(val name: String) {
        override fun toString() = name
    }
    
    val s = Example("s")
    val t = Example("t")
    
    @Test
    fun same() {
        assertTrue("s & t should be distinct instances") { s !== t }
        assertMatch(sameInstance(s)(s))
        assertMismatchWithDescription("was: t", sameInstance(s)(t))
    }

    @Test
    fun description() {
        assertEquals("is same instance as s", sameInstance(s).description)
        assertEquals("is not same instance as t", (!sameInstance(t)).description)
    }
}

class Nullability {
    @Test
    fun absence() {
        val m : Matcher<Int?> = absent()

        assertMatch(m(null))
        assertMismatchWithDescription("was: 100", m(100))
    }

    @Test
    fun presence() {
        val m : Matcher<String?> = present()

        assertMatch(m("xxx"))
        assertMismatchWithDescription("was: null", m(null))
    }

    @Test
    fun presence_and_constraint() {
        val m : Matcher<String?> = present(equalTo("xxx"))

        assertMatch(m("xxx"))
        assertMismatchWithDescription("was: null", m(null))
        assertMismatchWithDescription("was: \"yyy\"", m("yyy"))
    }

    @Test
    fun description() {
        val m : Matcher<String?> = absent()
        val n : Matcher<String?> = present()

        assertEquals("is null", m.description)
        assertEquals("is not null", n.description)
        val valueMatcher = equalTo("test")
        assertEquals("is not null & " + valueMatcher.description, present(valueMatcher).description)
    }
}

class Downcasting {
    val m = isA<String>(equalTo("bob"))

    @Test
    fun wrong_type() {
        val actual = 10.0
        
        assertMismatchWithDescription("was: a ${actual.platformSpecificTypeName}", m(actual))
    }
    
    @Test
    fun correct_type_and_downcast_mismatch() {
        assertMismatchWithDescription("was: \"alice\"", m("alice"))
    }

    @Test
    fun correct_type_and_downcast_match() {
        assertMatch(m("bob"))
    }

    @Test
    fun matching_type_only() {
        assertMatch(isA<String>()("bob"))
        assertMismatchWithDescription("was: a ${1.platformSpecificTypeName}", isA<String>()(1))
    }
    
    private val Any.platformSpecificTypeName get() = this::class.reportedName
}


class Comparables {
    @Test
    fun order_comparisons() {
        assertThat(10, greaterThan(5))
        assertThat(10, greaterThanOrEqualTo(5))
        assertThat(10, greaterThanOrEqualTo(10))
        assertThat(10, !greaterThanOrEqualTo(50))

        assertThat(10, lessThan(20))
        assertThat(10, lessThanOrEqualTo(20))
        assertThat(10, lessThanOrEqualTo(10))
        assertThat(10, !lessThanOrEqualTo(5))
    }

    @Test
    fun within_range() {
        assertThat(1, isWithin(1..20))
        assertThat(10, isWithin(1..20))
        assertThat(20, isWithin(1..20))
        assertThat(0, !isWithin(1..20))
        assertThat(21, !isWithin(1..20))

        assertThat(isWithin(1..20).description, equalTo("is within 1..20"))
        assertThat((!isWithin(1..20)).description, equalTo("is not within 1..20"))
    }
}

class ExampleException(message: String) : Exception(message)
class DifferentException(message: String) : Exception(message)

class Throwing {
    @Test
    fun matches_block_that_throws_specific_exception() {
        assertThat({throw ExampleException("testing")}, throws<ExampleException>())
    }

    @Test
    fun matches_block_that_throws_specific_exception_and_state() {
        assertThat({throw ExampleException("testing")},
                throws<ExampleException>(has(Exception::message, present(equalTo("testing")))))
    }

    @Test
    fun does_not_match_block_that_does_not_throw() {
        assertThat({}, ! throws<ExampleException>())
    }

    @Test
    fun does_not_match_block_that_throws_different_exception() {
        assertThat({throw DifferentException("xxx")}, !throws<ExampleException>())
    }
}

class Converting {

    data class HasProperty(val hasAProperty: Boolean)

    @Test
    fun create_from_a_property() {
        assertThat(HasProperty(true), Matcher(HasProperty::hasAProperty))
    }
}