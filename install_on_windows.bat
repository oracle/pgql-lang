cd pgql-spoofax
call mvn clean install

cd ..\graph-query-ir
call mvn clean install

cd ..\pgql-lang
mkdir src\main\resources\
copy ..\pgql-spoofax\target\pgqllang-1.1.0-SNAPSHOT.spoofax-language src\main\resources\pgql-1.1.spoofax-language
call mvn clean install

cd ..\pgql-tests
call mvn test
cd spring-boot-app
call mvn clean package
cd target
call java -jar pgql-spring-boot-app-1.0.0.jar
cd ..\..\..
