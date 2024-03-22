val scalaLibraryVersion = "2.13.13"
val gatlingVersion = "3.10.5"


configurations {
    "perfTestImplementation" {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
}


dependencies {
    "perfTestImplementation"("io.gatling.highcharts:gatling-charts-highcharts:$gatlingVersion")
    "perfTestImplementation"("io.gatling:gatling-core:$gatlingVersion")

    "perfTestCompileOnly"("org.scala-lang:scala-library:$scalaLibraryVersion")
}