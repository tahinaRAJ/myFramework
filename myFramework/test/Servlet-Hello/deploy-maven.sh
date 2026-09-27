#!/bin/bash
set -e

TOMCAT_WEBAPPS="/opt/tomcat11/webapps"
APP_NAME="MyFramework"

echo "1) Installation du framework dans le repo Maven local..."
(cd ../../framework && mvn -q clean install)

echo "2) Build du WAR de l'application de test..."
mvn -q clean package

WAR_FILE="target/${APP_NAME}.war"

if [ ! -f "$WAR_FILE" ]; then
    echo "Erreur : $WAR_FILE introuvable."
    exit 1
fi

if [ -d "$TOMCAT_WEBAPPS" ]; then
    cp -f "$WAR_FILE" "$TOMCAT_WEBAPPS/"
    echo "WAR déployé vers $TOMCAT_WEBAPPS"
else
    echo "Répertoire Tomcat non trouvé : $TOMCAT_WEBAPPS"
    echo "WAR généré : $WAR_FILE (à copier manuellement dans webapps/)"
fi

echo ""
echo "========================================="
echo "Déploiement terminé"
echo "Testez : http://localhost:8080/$APP_NAME/products/list"
echo "========================================="
