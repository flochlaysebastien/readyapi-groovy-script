# [SoapUI Extension Plugins](https://www.soapui.org/docs/extension-plugins.html)
Il est possible d'étendre les capacités de ReadyApi en ajoutant des **plugins/librairies/modules**.   

Dans notre cas, nous avons besoin d'jouter des "jar" afin d'ajouter des fonctionnalités à nos scripts Groovy.
Cette opération est décrite en détails dans la documentation SoapUI ["Extending soapUI"](https://www.soapui.org/extension-plugins/old-style-extensions/developing-old-style-extensions.html). 
Il suffit simplement de placer ces librairies dans le répertoire ReadyApi prévu à cet effet **_[/home/USER/SmartBear/ReadyAPI-2.5.0/bin/ext/]_**.

Les librairies à ajouter sont les suivantes :
 - amqp-client-5.6.0.jar (https://repo1.maven.org/maven2/com/rabbitmq/amqp-client/5.6.0/amqp-client-5.6.0.jar)
 - google-auth-library-credentials-0.17.1.jar (https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/0.17.1/google-auth-library-credentials-0.17.1.jar)
 - google-auth-library-oauth2-http-0.17.1.jar (https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/0.17.1/google-auth-library-oauth2-http-0.17.1.jar)
 - google-http-client-1.23.0.jar (https://repo1.maven.org/maven2/com/google/http-client/google-http-client/1.23.0/google-http-client-1.23.0.jar)
 - google-http-client-jackson2-1.32.1.jar (https://repo1.maven.org/maven2/com/google/http-client/google-http-client-jackson2/1.32.1/google-http-client-jackson2-1.32.1.jar)
 - guava-20.0.jar (https://repo1.maven.org/maven2/com/google/guava/guava/20.0/guava-20.0.jar)