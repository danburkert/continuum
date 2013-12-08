# continuum

continuum is a library for working with intervals over continuous, total-ordered domains in Scala.  The functionality is similar to Guava's [Range](https://code.google.com/p/guava-libraries/wiki/RangesExplained) library. Intervals may be grouped into interval sets which automatically coalesce overlapping intervals.

[![Build Status](https://travis-ci.org/danburkert/continuum.png)](https://travis-ci.org/danburkert/continuum)

## Interval
An interval is a non-empty, two sided bound over a continuous, infinite, total-ordered set of values. An interval contains all values between its lower and upper bound. Additionally, the upper or lower bound of the interval may be unbounded, in which case the interval contains all values above or below, respectively.  Intervals provide a rich interface of constructors and set-like operations:

```scala
scala> import continuum.Interval
import continuum.Interval

// Intervals can be closed or open on each side
scala> Interval.closedOpen(10, 20)
res0: continuum.Interval[Int] = [10, 20)

// Intervals can be made from any Ordered element
scala> Interval.closed("bar", "baz")
res1: continuum.Interval[String] = [bar, baz]

// Intervals can be unbounded above or below
scala> Interval.greaterThan(19.68)
res2: continuum.Interval[Double] = (19.68, ∞)

scala> Interval.atMost(-42)
res3: continuum.Interval[Int] = (-∞, -42]

scala> Interval.all[Int]
res4: continuum.Interval[Int] = (-∞, ∞)

// Intervals may be a single point
scala> Interval.point(19)
res5: continuum.Interval[Int] = [19]

scala> Interval.closed(19, 19)
res6: continuum.Interval[Int] = [19]

// Intervals may not be empty
scala> Interval.openClosed(1, 1)
java.lang.IllegalArgumentException

scala> Interval.open(1, 1)
java.lang.IllegalArgumentException

// Tuples may be implicitly converted to Intervals
scala> val fromTuple: Interval[String] = ("a", "z")
fromTuple: continuum.Interval[String] = [a, z)

// Ranges may be implicitly converted to an Interval
scala> val fromRange: Interval[Int] = 1 until 10
fromRange: continuum.Interval[Int] = [1, 10)

// and converted explicitly back to a Range
scala> fromRange.toRange
res9: Range = Range(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)

// Intervals may be intersected
scala> Interval.open("aardvark", "camel") intersect Interval.closed("bear", "deer")
res10: Option[continuum.Interval[String]] = Some([bear, camel))

// or unioned
scala> Interval.open("aardvark", "camel") union Interval.closed("bear", "deer")
res11: Option[continuum.Interval[String]] = Some((aardvark, deer])

// or the minimum spanning interval
scala> Interval.lessThan(0) span Interval.open(20, 25)
res12: continuum.Interval[Int] = (-∞, 25)
```

## IntervalSet

An interval set is a set which contains 0 or more intervals. Connected intervals are automatically coalesced, so at all times an interval set contains only the minimum number of intervals necessary. Interval sets are immutable and persistent, and support the full Scala Set API.

```scala

scala> import continuum.Interval; import continuum.IntervalSet
import continuum.Interval
import continuum.IntervalSet

scala> IntervalSet(Interval.open(10, 20))
res0: continuum.IntervalSet[Int] = IntervalSet((10, 20))

scala> IntervalSet(Interval.open(10, 20)) + Interval.closed(15, 25)
res1: continuum.IntervalSet[Int] = IntervalSet((10, 25])

scala> IntervalSet(Interval.open(10, 20)) + Interval.closed(25, 30)
res2: continuum.IntervalSet[Int] = IntervalSet((10, 20), [25, 30])

scala> IntervalSet(1 to 10) intersect IntervalSet(5 to 15)
res3: continuum.IntervalSet[Int] = IntervalSet([5, 10])

scala> IntervalSet(1 to 10).complement
res4: continuum.IntervalSet[Int] = IntervalSet((-∞, 1), (10, ∞))

scala> IntervalSet(Interval.all[Int]) - Interval.closed(32, 35)
res5: continuum.IntervalSet[Int] = IntervalSet((-∞, 32), (35, ∞))
```

## License

Copyright © 2013 Dan Burkert

Distributed under the Apache License, Version 2.0
