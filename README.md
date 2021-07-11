# curl xml

## Building
```
./gradlew clean build
```

## Running
### soap-server (https://github.com/mike-seger/soap-server)
```
./curl-xml "https://ms-soap-server.herokuapp.com/ws" \
    <<<'<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:gs="http://net128.com/soap-server">
   <soapenv:Header/>
   <soapenv:Body>
      <gs:countryNameList>
         <gs:name>Spain</gs:name>
         <gs:name>Germany</gs:name>
      </gs:countryNameList>
   </soapenv:Body>
</soapenv:Envelope>'
```

### BLZ service
```
# Get input from file
./curl-xml -H "SOAPAction: blz:getBank" "http://www.thomas-bayer.com/axis2/services/BLZService" \
    -d @src/test/resources/thomas-bayer.com/request.xml

# Get input from CSV file and transform result
./curl-xml -inCSV -inTR=src/test/resources/thomas-bayer.com/xsl/2-blz-request.xsl \
    -outTR=src/test/resources/thomas-bayer.com/xsl/blc-extract.xsl \
    -X POST -H 'Content-Type: text/xml' \
    -H 'SOAPAction: blz:getBank' \
    'http://www.thomas-bayer.com/axis2/services/BLZService' \
    <src/test/resources/thomas-bayer.com/blz.csv

# Alternative: Get input from stdin
<<<$(cat src/test/resources/thomas-bayer.com/blz.csv)

# Alternative: Get input from pipe
cat src/test/resources/thomas-bayer.com/blz.csv | ./curl-xml 
```
