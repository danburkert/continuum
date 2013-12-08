package continuum

import org.scalacheck.{Shrink, Gen, Arbitrary}
import org.scalacheck.Arbitrary.arbitrary

import continuum.bound.{Open, Closed, Unbounded}
import scala.collection.immutable.SortedSet

trait Generators {

  implicit def arbOpenBound[T <% Ordered[T] : Arbitrary]: Arbitrary[Open[T]] =
    Arbitrary { for (cut <- arbitrary[T]) yield Open(cut) }

  implicit def arbClosedBound[T <% Ordered[T] : Arbitrary]: Arbitrary[Closed[T]] =
    Arbitrary { for (cut <- arbitrary[T]) yield Closed(cut) }

  implicit def arbUnbounded[T <% Ordered[T]]: Arbitrary[Unbounded[T]] = Arbitrary(Unbounded[T]())

  implicit def arbBound[T <% Ordered[T] : Arbitrary]: Arbitrary[Bound[T]] =
    Arbitrary(Gen.frequency(
      4 -> arbitrary[Open[T]],
      4 -> arbitrary[Closed[T]],
      1 -> arbitrary[Unbounded[T]]))

  implicit def arbLesserRay[T <% Ordered[T] : Arbitrary]: Arbitrary[LesserRay[T]] =
    Arbitrary(for (b <- arbitrary[Bound[T]]) yield LesserRay(b))

  implicit def arbGreaterRay[T <% Ordered[T] : Arbitrary]: Arbitrary[GreaterRay[T]] =
    Arbitrary(for (b <- arbitrary[Bound[T]]) yield GreaterRay(b))

  implicit def arbRay[T <% Ordered[T] : Arbitrary]: Arbitrary[Ray[T]] =
      Arbitrary(Gen.oneOf(arbitrary[GreaterRay[T]], arbitrary[LesserRay[T]]))

  implicit def arbInterval[T <% Ordered[T]: Arbitrary]: Arbitrary[Interval[T]] = Arbitrary {
    for {
      a <- arbitrary[GreaterRay[T]]
      b <- arbitrary[LesserRay[T]] if Interval.validate(a, b)
    } yield Interval(a, b)
  }

  implicit def arbIntInterval: Arbitrary[Interval[Int]] = Arbitrary {
    def genOpenAbove(below: Bound[Int]): Gen[Bound[Int]] = below match {
      case Closed(l)   if l == Int.MaxValue => arbitrary[Unbounded[Int]]
      case Open(l)     if l == Int.MaxValue => arbitrary[Unbounded[Int]]
      case Closed(l)   => for(n <- Gen.choose(l + 1, Int.MaxValue)) yield Open(n)
      case Open(l)     => for(n <- Gen.choose(l + 1, Int.MaxValue)) yield Open(n)
      case Unbounded() => arbitrary[Open[Int]]
    }

    def genClosedAbove(below: Bound[Int]): Gen[Bound[Int]] = below match {
      case Open(l)     if l == Int.MaxValue => arbitrary[Unbounded[Int]]
      case Closed(l)   => for(n <- Gen.choose(l, Int.MaxValue)) yield Closed(n)
      case Open(l)     => for(n <- Gen.choose(l + 1, Int.MaxValue)) yield Closed(n)
      case Unbounded() => arbitrary[Closed[Int]]
    }

    def genLesserRay(g: GreaterRay[Int]): Gen[LesserRay[Int]] = Gen.frequency(
      6 -> genOpenAbove(g.bound),
      6 -> genClosedAbove(g.bound),
      1 -> arbitrary[Unbounded[Int]]
    ).map(LesserRay(_))

    for {
      lower <- arbitrary[GreaterRay[Int]]
      upper <- genLesserRay(lower)
    } yield new Interval(lower, upper)
  }

  implicit def shrinkGreaterRay[T <% Ordered[T] : Shrink]: Shrink[GreaterRay[T]] = Shrink { ray =>
    ray.bound match {
      case Closed(n)   => for (np <- Shrink.shrink(n)) yield GreaterRay(Closed(np))
      case Open(n)     => for (np <- Shrink.shrink(n)) yield GreaterRay(Open(np))
      case Unbounded() => Stream.empty
    }
  }

  implicit def shrinkLesserRay[T <% Ordered[T] : Shrink]: Shrink[LesserRay[T]] = Shrink { ray =>
    ray.bound match {
      case Closed(n)   => for (np <- Shrink.shrink(n)) yield LesserRay(Closed(np))
      case Open(n)     => for (np <- Shrink.shrink(n)) yield LesserRay(Open(np))
      case Unbounded() => Stream.empty
    }
  }

  implicit def shrinkInterval[T <% Ordered[T] : Shrink]: Shrink[Interval[T]] = Shrink { interval =>
    for {
      lower <- Shrink.shrink(interval.lower)
      upper <- Shrink.shrink(interval.upper) if Interval.validate(lower, upper)
    } yield new Interval(lower, upper)
  }

  implicit def arbRange: Arbitrary[Range] = Arbitrary {
    Gen.sized { size =>
      for {
        lower <- arbitrary[Int] if lower + size >= lower
      } yield Range.inclusive(lower, lower + size)
    }
  }

  def genOpen: Gen[Interval[Int]] = Gen.sized { size =>
    for {
      lower <- arbitrary[Int] if lower + size > lower
    } yield Interval.open(lower, lower + size)
  }

  def genClosed: Gen[Interval[Int]] = Gen.sized { size =>
    for {
      lower <- arbitrary[Int] if lower + size >= lower
    } yield Interval.closed(lower, lower + size)
  }

  def genOpenClosed: Gen[Interval[Int]] = Gen.sized { size =>
    for {
      lower <- arbitrary[Int] if lower + size > lower
    } yield Interval.openClosed(lower, lower + size)
  }

  def genClosedOpen: Gen[Interval[Int]] = Gen.sized { size =>
    for {
      lower <- arbitrary[Int] if lower + size > lower
    } yield Interval.closedOpen(lower, lower + size)
  }

  def closedUnbounded: Gen[Interval[Int]] =
    Gen.sized(size => Interval.atLeast(Int.MaxValue - size))

  def openUnbounded: Gen[Interval[Int]] =
    Gen.sized(size => Interval.greaterThan(Int.MaxValue - size))

  def unboundedClosed: Gen[Interval[Int]] =
    Gen.sized(size => Interval.atMost(Int.MinValue + size))

  def unboundedOpen: Gen[Interval[Int]] =
    Gen.sized(size => Interval.lessThan(Int.MinValue + size))

  def genIntervalRange = Gen.frequency(
      4 -> genOpen,
      4 -> genClosed,
      4 -> genOpenClosed,
      4 -> genClosedOpen,
      1 -> closedUnbounded,
      1 -> openUnbounded,
      1 -> unboundedClosed,
      1 -> unboundedOpen)

  implicit def arbIntervalSet[T <% Ordered[T] : Arbitrary]: Arbitrary[IntervalSet[T]] =
    Arbitrary(for (intervals <- arbitrary[Array[Interval[T]]]) yield IntervalSet(intervals:_*))

  implicit def arbIntIntervalSet: Arbitrary[IntervalSet[Int]] =
    Arbitrary(for (intervals <- arbitrary[Array[Interval[Int]]]) yield IntervalSet(intervals:_*))
}
