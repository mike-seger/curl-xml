# curl xml

## Running
```
# Get input from stdin
./gradlew run --args="-X POST -H 'Content-Type: text/xml' \
-H 'SOAPAction: blz:getBank' \
'http://www.thomas-bayer.com/axis2/services/BLZService' \
" <src/test/resources/request.xml

# Alternative: Get input from pipe
<<<$(cat src/test/resources/request.xml)

# Alternative: Get input from pipe
cat src/test/resources/request.xml | ./gradlew run...
```
