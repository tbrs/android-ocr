buildscript {
    val kotlinVersion ="1.3.20"

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.0-rc02")
        classpath(kotlin("gradle-plugin", kotlinVersion))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
