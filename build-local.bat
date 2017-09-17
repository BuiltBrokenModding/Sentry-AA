@echo off
gradlew clean build publish -PbambooshortPlanName=BBM-Dev -Plocal=true --refresh-dependencies
echo "Exit?"
pause > nul