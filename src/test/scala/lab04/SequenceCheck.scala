package scala.lab04

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Properties}

import scala.lab04.Sequences.*
import scala.lab04.Sequences.Sequence.*

object SequenceCheck extends Properties("Sequence"):

  // define a recursive generator of lists, monadically
  def sequenceGen[A: Arbitrary](): Gen[Sequence[A]] = for
    i <- arbitrary[A]
    b <- Gen.prob(0.8)
    s <- if b then sequenceGen().map(s2 => Cons(i, s2)) else Gen.const(Nil())
  yield s

  // define custom arbitrary lists and mappers
  given intSeqArbitrary: Arbitrary[Sequence[Int]] = Arbitrary(sequenceGen[Int]())

  // check axioms, universally
  property("mapAxioms") =
    forAll: (seq: Sequence[Int], f: Int => Int) =>
      //println(seq); println(f(10)) // inspect what's using
      (seq, f) match
        case (Nil(), f) =>  map(Nil())(f) == Nil()
        case (Cons(h, t), f) => map(Cons(h, t))(f) == Cons(f(h), map(t)(f))

  property("filterAxioms") =
    forAll: (seq: Sequence[Int], p: Int => Boolean) =>
      (seq, p) match
        case (Nil(), p) => filter(Nil())(p) == Nil()
        case (Cons(h, t), p) if p(h) => filter(Cons(h, t))(p) == Cons(h, filter(t)(p))
        case (Cons(h, t), p) => filter(Cons(h, t))(p) == filter(t)(p)

  property("sumAxioms") =
    forAll: (seq: Sequence[Int]) =>
      seq match
        case Cons(head, tail) => sum(Cons(head, tail)) == head + sum(tail)
        case Nil() => sum(Nil()) == 0

  property("concatAxioms") =
    forAll: (seq: Sequence[Int], seq2: Sequence[Int]) =>
      (seq, seq2) match
        case (Nil(), seq2) => concat(seq)(seq2) == seq2
        case (Cons(h, t), seq2) => concat(seq)(seq2) == Cons(h, concat(t)(seq2))
      
  property("flatMapAxioms") =
    forAll: (seq: Sequence[Int], f: Int => Sequence[Int]) =>
      (seq, f) match
        case (Nil(), f) => flatMap(seq)(f) == Nil()
        case (Cons(h, t), f) => flatMap(seq)(f) == concat(f(h))(t.flatMap(f))      

  // how to check a generator works as expected
  @main def showSequences() =
    Range(0,20).foreach(i => println(summon[Arbitrary[Sequence[Int]]].arbitrary.sample))