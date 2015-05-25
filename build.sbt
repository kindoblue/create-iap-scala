name := "create-iap-scala"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.bouncycastle" % "bcprov-jdk15on" % "1.50",
  "org.bouncycastle" % "bcmail-jdk15on" % "1.50",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.50",
  "joda-time" % "joda-time" % "2.7",
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.clapper" %% "argot" % "1.0.3"

)