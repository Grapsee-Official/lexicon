@if "%DEBUG%"=="" @echo off
@rem Gradle wrapper bootstrap for Windows
java -classpath "%~dp0\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
