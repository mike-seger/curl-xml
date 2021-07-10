# curl xml

## Running
```
# Get input from stdin
./gradlew run --args="-inCSV \
    -inTR=src/test/resources/xsl/2-blz-request.xsl \
    -outTR=src/test/resources/xsl/blc-extract.xsl \
    -X POST -H 'Content-Type: text/xml' \
    -H 'SOAPAction: blz:getBank' \
    'http://www.thomas-bayer.com/axis2/services/BLZService' \
    " <src/test/resources/blz.csv

# Alternative: Get input from pipe
<<<$(cat src/test/resources/blz.csv)

# Alternative: Get input from pipe
cat src/test/resources/blz.csv | ./gradlew run...
```
