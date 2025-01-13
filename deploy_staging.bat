@echo off
set profile=staging
set ipAddress=185.135.30.120
set username=rocky
set password=mahdi

echo start build %profile% without run test ...
call mvn -P%profile% -Dmaven.test.skip.exec=true clean install
echo start transfer webApp to server %ipAddress%
pscp -i "E:\keys\khodam\privateKey.ppk" hub-web/target/hub-web.jar %username%@%ipAddress%:/tmp