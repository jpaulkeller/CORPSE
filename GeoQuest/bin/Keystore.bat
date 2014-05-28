REM http://java.sun.com/j2se/1.3/docs/tooldocs/win32/keytool.html

echo %JAVA_HOME%

set JAVA=C:\pkgs\Java\jdk1.6.0_11\bin\
%JAVA%\keytool -genkey -keystore C:\pkgs\dist\keystore.pal -dname "cn=Palantiri" -alias palantiri -validity 365 -storepass vgy78uhb -keypass vgy78uhb
%JAVA%\keytool -list -keystore C:\pkgs\dist\keystore.pal -storepass vgy78uhb 
pause
