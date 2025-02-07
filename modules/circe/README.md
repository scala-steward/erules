# Erules Circe
The purpose of this module is to provid `Encoder` instances of `erules` types
and the `JsonReportEncoder` instances to produce a json report.

**Sbt**
```sbt
  libraryDependencies += "com.github.geirolz" %% "erules-core" % "0.0.6"
  libraryDependencies += "com.github.geirolz" %% "erules-circe" % "0.0.6"
```

### Usage

Given these data classes
```scala
case class Country(value: String)
case class Age(value: Int)

case class Citizenship(country: Country)
case class Person(
  name: String,
  lastName: String,
  age: Age,
  citizenship: Citizenship
)
```

Let's write the rules!
```scala
import erules.core.Rule
import erules.core.RuleVerdict.*
import cats.data.NonEmptyList
import cats.Id

val checkCitizenship: Rule[Id, Citizenship] =
  Rule("Check UK citizenship").apply[Id, Citizenship]{
    case Citizenship(Country("UK")) => Allow.withoutReasons
    case _                          => Deny.because("Only UK citizenship is allowed!")
  }
// checkCitizenship: Rule[Id, Citizenship] = RuleImpl(<function1>,Check UK citizenship,None,None)

val checkAdultAge: Rule[Id, Age] =
  Rule("Check Age >= 18").apply[Id, Age] {
    case a: Age if a.value >= 18  => Allow.withoutReasons
    case _                        => Deny.because("Only >= 18 age are allowed!")
  }
// checkAdultAge: Rule[Id, Age] = RuleImpl(<function1>,Check Age >= 18,None,None)

val allPersonRules: NonEmptyList[Rule[Id, Person]] = NonEmptyList.of(
  checkCitizenship
    .targetInfo("citizenship")
    .contramap(_.citizenship),
  checkAdultAge
    .targetInfo("age")
    .contramap(_.age)
)
// allPersonRules: NonEmptyList[Rule[Id, Person]] = NonEmptyList(RuleImpl(scala.Function1$$Lambda$10486/0x00000008029eb390@1ea18d05,Check UK citizenship,None,Some(citizenship)), RuleImpl(scala.Function1$$Lambda$10486/0x00000008029eb390@531ca4f4,Check Age >= 18,None,Some(age)))
```

Import 
```scala
import erules.circe.implicits.*
```

And `circe-generic` to derive the `Person` encoder automatically
```scala
import io.circe.generic.auto.*
```

And create the JSON report
```scala
import erules.core.*
import erules.implicits.*
import erules.circe.implicits.*

import cats.effect.IO
import cats.effect.unsafe.implicits.*

val person: Person = Person("Mimmo", "Rossi", Age(16), Citizenship(Country("IT")))
// person: Person = Person(Mimmo,Rossi,Age(16),Citizenship(Country(IT)))

val result: IO[EngineResult[Person]]  = for {
  engine <- RulesEngine[IO].withRules[Id, Person](allPersonRules).denyAllNotAllowed
  result <- engine.parEval(person)
} yield result
// result: IO[EngineResult[Person]] = IO(...)

//yolo
result.unsafeRunSync().asJsonReport
// res0: io.circe.Json = {
//   "data" : {
//     "name" : "Mimmo",
//     "lastName" : "Rossi",
//     "age" : {
//       "value" : 16
//     },
//     "citizenship" : {
//       "country" : {
//         "value" : "IT"
//       }
//     }
//   },
//   "verdict" : {
//     "type" : "Denied",
//     "evaluatedRules" : [
//       {
//         "rule" : {
//           "name" : "Check UK citizenship",
//           "targetInfo" : "citizenship",
//           "fullDescription" : "Check UK citizenship for citizenship"
//         },
//         "verdict" : {
//           "type" : "Deny",
//           "reasons" : [
//             "Only UK citizenship is allowed!"
//           ]
//         },
//         "executionTime" : {
//           "length" : 125334,
//           "unit" : "NANOSECONDS"
//         }
//       },
//       {
//         "rule" : {
//           "name" : "Check Age >= 18",
//           "targetInfo" : "age",
//           "fullDescription" : "Check Age >= 18 for age"
//         },
//         "verdict" : {
//           "type" : "Deny",
//           "reasons" : [
//             "Only >= 18 age are allowed!"
//           ]
//         },
//         "executionTime" : {
//           "length" : 56500,
//           "unit" : "NANOSECONDS"
//         }
//       }
//     ]
//   }
// }
```
