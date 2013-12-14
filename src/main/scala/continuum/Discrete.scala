package continuum

/**
 * A trait for describing discrete domains.
 */
trait Discrete[T] {
  def next(value: T): Option[T]
}

object Discrete {
  /**
   * An implementation of the Discrete trait for longs.
   */
  implicit object DiscreteLong extends Discrete[Long] {
    override def next(long: Long): Option[Long] = if (long == Long.MaxValue) None else Some(long + 1)
  }

  /**
   * An implementation of the Discrete trait for ints.
   */
  implicit object DiscreteInt extends Discrete[Int] {
    override def next(int: Int): Option[Int] = if (int == Int.MaxValue) None else Some(int + 1)
  }
}
