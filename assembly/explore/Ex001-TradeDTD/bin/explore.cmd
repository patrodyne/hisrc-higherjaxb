@echo off
@rem Run a Maven goal to execute the Explorer class.
@mvn test-compile exec:java ^
	-Dexec.classpathScope=test ^
	-Dexec.mainClass=org.patrodyne.jvnet.higherjaxb.ex001.Explorer


