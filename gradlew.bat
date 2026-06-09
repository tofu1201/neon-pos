echo Gradle is not installed on this machine. Install Gradle or add the standard Gradle wrapper jar.
@echo off
set DIR=%~dp0
set JAR_DIR=%DIR%gradle\wrapper
set CLASSPATH=%JAR_DIR%\gradle-wrapper.jar;%JAR_DIR%\gradle-wrapper-shared-8.9.jar
java -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
