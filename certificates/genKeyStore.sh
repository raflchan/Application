#!/bin/sh

password="nopassword"
rm all.pem > /dev/null
rm rafl.cf.jks  > /dev/null
rm cert_and_key.p12  > /dev/null
cat cert.pem chain.pem fullchain.pem > all.pem
openssl pkcs12 -export -in all.pem -inkey privkey.pem -out cert_and_key.p12 -name app.rafl.cf -CAfile chain.pem -caname root -password pass:${password}
keytool -importkeystore -deststorepass ${password} -destkeypass ${password} -destkeystore rafl.cf.jks -srckeystore cert_and_key.p12 -srcstoretype PKCS12 -srcstorepass ${password} -alias app.rafl.cf -deststoretype pkcs12
keytool -import -trustcacerts -alias root -file chain.pem -keystore rafl.cf.jks -storepass ${password}
