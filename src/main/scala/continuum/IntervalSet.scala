package continuum

import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.SortedSet
import scala.collection.{GenSet, mutable, SortedSetLike}

/**
 * A set containing 0 or more intervals. Intervals which may be unioned together are automatically
 * coalesced, so at all times an interval set contains the minimum number of necessary intervals.
 * Interval sets are immutable and persistent.
 */
final class IntervalSet[T <% Ordered[T]](override val seq: SortedSet[Interval[T]])
  extends SortedSet[Interval[T]] with SortedSetLike[Interval[T], IntervalSet[T]] {

  override def empty: IntervalSet[T] = IntervalSet.empty[T]

  override def iterator: Iterator[Interval[T]] = seq.iterator

  override def size: Int = seq.size

  override def contains(interval: Interval[T]): Boolean =
    dropLesser(interval).headOption.fold(false)(_ encloses interval)

  override def +(interval: Interval[T]): IntervalSet[T] = {
    val unionables: IntervalSet[T] = unioning(interval)
    val union = unionables.foldLeft(interval)((a, b) => (a union b).get)
    new IntervalSet(seq -- unionables + union)
  }

  override def -(interval: Interval[T]): IntervalSet[T] = {
    val intersectings = intersecting(interval)
    val differences = intersectings.flatMap(_ difference interval)
    new IntervalSet(seq -- intersectings ++ differences)
  }

  override def intersect(other: GenSet[Interval[T]]): IntervalSet[T] =
    other.foldLeft(this)(_ intersect _)

  implicit def ordering: Ordering[Interval[T]] = Ordering.ordered

  override def rangeImpl(from: Option[Interval[T]], until: Option[Interval[T]]): IntervalSet[T] =
    (from, until) match {
      case (Some(f), Some(u)) => intersect(f.span(u))
      case (Some(f), None) => new IntervalSet(dropLesser(f))
      case (None, Some(u)) => intersect(u.lesser.fold(u)(_ span u))
      case (None, None) => this
    }

  override def stringPrefix: String = "IntervalSet"

  private def dropLesser(interval: Interval[T]): SortedSet[Interval[T]] =
    interval.lesser.fold(seq)(seq.from)

  /**
   * Tests if the provided interval intersects with any of the intervals in this set.
   */
  def intersects(interval: Interval[T]): Boolean = dropLesser(interval).head intersects interval

  /**
   * Returns the subset of intervals which intersect with the given interval.
   */
  def intersecting(interval: Interval[T]): IntervalSet[T] =
    new IntervalSet(dropLesser(interval).takeWhile(_ intersects interval))

  /**
   * Returns the the result of the intervals in this set intersected with the given interval.
   */
  def intersect(interval: Interval[T]): IntervalSet[T] =
    new IntervalSet(SortedSet.empty[Interval[T]] ++
                    intersecting(interval).flatMap(_ intersect interval))

  /**
   * Alias for `intersect`.
   */
  def &(interval: Interval[T]): IntervalSet[T] = intersect(interval)

  /**
   * Returns the subset of intervals which union with the given interval.
   */
  def unioning(interval: Interval[T]): IntervalSet[T] =
    new IntervalSet(dropLesser(interval).takeWhile(_ unions interval))

  def span: Interval[T] = seq.head span seq.last

  def complement: IntervalSet[T] = IntervalSet(Interval.all[T]) -- this
}

object IntervalSet extends {

  def empty[T <% Ordered[T]] = new IntervalSet(SortedSet.empty[Interval[T]])

  def apply[T <% Ordered[T]](intervals: Interval[T]*): IntervalSet[T] =
    intervals.foldLeft(empty[T])(_ + _)

  def newBuilder[T <% Ordered[T]]: mutable.Builder[Interval[T], IntervalSet[T]] =
    new mutable.SetBuilder[Interval[T], IntervalSet[T]](empty[T])

  implicit def canBuildFrom[T <% Ordered[T]]
      : CanBuildFrom[IntervalSet[_], Interval[T], IntervalSet[T]] =
    new CanBuildFrom[IntervalSet[_], Interval[T], IntervalSet[T]] {
      def apply(from: IntervalSet[_]): mutable.Builder[Interval[T], IntervalSet[T]] = newBuilder[T]
      def apply(): mutable.Builder[Interval[T], IntervalSet[T]] = newBuilder[T]
    }
}
