#!/bin/bash

APP_NAME="MyFramework"
SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp"
BUILD_DIR="build"
LIB_DIR="lib"

FRAMEWORK_DIR="../../framework"
FRAMEWORK_JAR_SOURCE="$FRAMEWORK_DIR/target/my-framework-1.0-SNAPSHOT.jar"
FRAMEWORK_JAR_TARGET="$LIB_DIR/my-framework.jar"

TOMCAT_WEBAPPS="/opt/tomcat11/webapps"
JAKARTA_SERVLET_API="/opt/tomcat11/lib/servlet-api.jar"

echo "Vérification du framework JAR..."

if [ ! -f "$FRAMEWORK_JAR_TARGET" ]; then
    echo "Framework JAR manquant dans lib/, recherche..."
    if [ -f "$FRAMEWORK_JAR_SOURCE" ]; then
        echo "Framework JAR trouvé"
        cp "$FRAMEWORK_JAR_SOURCE" "$FRAMEWORK_JAR_TARGET"
    else
        echo "Framework JAR non trouvé dans $FRAMEWORK_JAR_SOURCE"
        echo "Compilez d'abord le framework :"
        echo "   cd ../../framework"
        echo "   mvn clean install"
        exit 1
    fi
else
    echo "Framework JAR déjà présent"
fi

if [ ! -f "$JAKARTA_SERVLET_API" ]; then
    echo "Erreur: $JAKARTA_SERVLET_API manquant"
    exit 1
fi

echo "Nettoyage..."
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/WEB-INF/classes
mkdir -p $BUILD_DIR/WEB-INF/lib

if [ -d "$SRC_DIR" ] && [ "$(ls -A $SRC_DIR 2>/dev/null)" ]; then
    echo "Compilation des sources..."
    find $SRC_DIR -name "*.java" > sources.txt 2>/dev/null
    if [ -s sources.txt ]; then
        javac -cp "$FRAMEWORK_JAR_TARGET:$JAKARTA_SERVLET_API" \
              -d $BUILD_DIR/WEB-INF/classes @sources.txt
        if [ $? -eq 0 ]; then
            echo "Compilation réussie"
        else
            echo "Erreurs de compilation"
        fi
    fi
    rm -f sources.txt
fi

cp $FRAMEWORK_JAR_TARGET $BUILD_DIR/WEB-INF/lib/

if [ -d "$WEB_DIR" ]; then
    cp -r $WEB_DIR/* $BUILD_DIR/
else
    echo "Erreur: $WEB_DIR n'existe pas"
    exit 1
fi

echo "Création du WAR..."
cd $BUILD_DIR
jar -cvf $APP_NAME.war * > /dev/null
cd ..

if [ -d "$TOMCAT_WEBAPPS" ]; then
    cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/
    echo "WAR déployé vers $TOMCAT_WEBAPPS"
else
    echo "Répertoire Tomcat non trouvé: $TOMCAT_WEBAPPS"
    echo "WAR généré: $BUILD_DIR/$APP_NAME.war"
    echo "Copiez-le manuellement dans votre dossier Tomcat/webapps/"
fi

echo ""
echo "========================================="
echo "Déploiement terminé"
echo "Testez: http://localhost:8080/$APP_NAME/"
echo "========================================="
