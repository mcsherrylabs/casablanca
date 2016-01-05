package sss.casablanca.util

trait JsonMapper[F, T] {

  def from(f: F): T
  def to(t: T): F

}