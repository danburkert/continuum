organization := "danburkert"

name := "continuum"

version := "0.4-SNAPSHOT"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
