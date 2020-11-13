name := "flink"
version := "1.0"
scalaVersion := "2.12.12"
scalacOptions += "-Ypartial-unification"

libraryDependencies ++=
  Seq("com.lihaoyi"           %% "upickle"       % "1.2.2",
      "org.scalatest"         %% "scalatest"     % "3.0.5" % "test",
      "org.apache.flink"      %% "flink-scala"   % "1.11.2",
      "org.apache.flink"      %% "flink-streaming-scala" % "1.11.2",
      "org.apache.flink"      %% "flink-connector-kafka" % "1.11.2",
      "org.apache.flink"      %% "flink-clients" % "1.11.2",
      "org.apache.flink"      %  "flink-core"    % "1.11.2",
      "org.apache.flink"      %  "flink-table"   % "1.11.2")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}
