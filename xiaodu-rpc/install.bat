call mvn clean
call mvn install -Dmaven.test.skip
call mvn dependency:copy-dependencies -DoutputDirectory=./target/release-jars -DincludeScope=compile
@pause