package erules.core

import cats.data.NonEmptyList
import cats.Id
import erules.core.RuleVerdict.{Allow, Deny}
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EngineResultSpec extends AnyWordSpec with Matchers with EitherValues {

  "EngineResult.combine" should {

    "Allow-Allow | combine two EngineResult creating a new EngineResult with the specified data" in {

      case class Foo(value: String)

      val rule1: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Allow.withoutReasons
      }

      val rule2: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Allow.withoutReasons
      }

      val er1 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Allow.because("R1")))
          )
        )
      )

      val er2 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule2, Right(RuleVerdict.Allow.because("R2")))
          )
        )
      )

      EngineResult.combine(Foo("TEST"), er1, er2) shouldBe EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Allow.because("R1"))),
            RuleResult(rule2, Right(RuleVerdict.Allow.because("R2")))
          )
        )
      )
    }

    "Allow-Deny | combine two EngineResult creating a new EngineResult with the specified data" in {

      case class Foo(value: String)

      val rule1: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Allow.withoutReasons
      }

      val rule2: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Deny.withoutReasons
      }

      val er1 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Allow.because("R1")))
          )
        )
      )

      val er2 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Denied(
          NonEmptyList.of(
            RuleResult(rule2, Right(RuleVerdict.Deny.because("R2")))
          )
        )
      )

      EngineResult.combine(Foo("TEST"), er1, er2) shouldBe EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Denied(
          NonEmptyList.of(
            RuleResult(rule2, Right(RuleVerdict.Deny.because("R2")))
          )
        )
      )
    }

    "Deny-Allow | combine two EngineResult creating a new EngineResult with the specified data" in {

      case class Foo(value: String)

      val rule1: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Deny.withoutReasons
      }

      val rule2: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Allow.withoutReasons
      }

      val er1 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Denied(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Deny.because("R1")))
          )
        )
      )

      val er2 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule2, Right(RuleVerdict.Allow.because("R2")))
          )
        )
      )

      EngineResult.combine(Foo("TEST"), er1, er2) shouldBe EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Denied(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Deny.because("R1")))
          )
        )
      )
    }

    "Deny-Deny | combine two EngineResult creating a new EngineResult with the specified data" in {

      case class Foo(value: String)

      val rule1: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Deny.withoutReasons
      }

      val rule2: PureRule[Foo] = Rule("Check Foo").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Deny.withoutReasons
      }

      val er1 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Denied(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Deny.because("R1")))
          )
        )
      )

      val er2 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Denied(
          NonEmptyList.of(
            RuleResult(rule2, Right(RuleVerdict.Deny.because("R2")))
          )
        )
      )

      EngineResult.combine(Foo("TEST"), er1, er2) shouldBe EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Denied(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Deny.because("R1"))),
            RuleResult(rule2, Right(RuleVerdict.Deny.because("R2")))
          )
        )
      )
    }

  }

  "EngineResult.combineAll" should {
    "combine all EngineResult creating a new EngineResult with the specified data" in {

      case class Foo(value: String)

      val rule1: PureRule[Foo] = Rule("Check Foo 1").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Allow.withoutReasons
      }

      val rule2: PureRule[Foo] = Rule("Check Foo 2").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Allow.withoutReasons
      }

      val rule3: PureRule[Foo] = Rule("Check Foo 3").partially[Id, Foo] {
        case Foo("")     => Deny.because("Empty Value")
        case Foo("TEST") => Allow.withoutReasons
      }

      val er1 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Allow.because("R1")))
          )
        )
      )

      val er2 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule2, Right(RuleVerdict.Allow.because("R2")))
          )
        )
      )

      val er3 = EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule3, Right(RuleVerdict.Allow.because("R3")))
          )
        )
      )

      EngineResult.combineAll(Foo("TEST"), er1, er2, er3) shouldBe EngineResult(
        data = Foo("TEST"),
        verdict = RuleResultsInterpreterVerdict.Allowed(
          NonEmptyList.of(
            RuleResult(rule1, Right(RuleVerdict.Allow.because("R1"))),
            RuleResult(rule2, Right(RuleVerdict.Allow.because("R2"))),
            RuleResult(rule3, Right(RuleVerdict.Allow.because("R3")))
          )
        )
      )
    }
  }

}
