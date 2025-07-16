@echo off
set profile=dev
set ipAddress=192.168.211.128
set username=root
set password=123456

echo start build %profile% without run test ...
call mvn -P%profile% -Dmaven.test.skip.exec=true clean install
echo start transfer webApp to server %ipAddress%
pscp  -pw %password% gold-wallet-web/target/gold-web.jar %username%@%ipAddress%:/tmp