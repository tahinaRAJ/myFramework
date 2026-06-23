package dev.framework.util;

import java.util.ArrayList;
import java.util.List;

import dev.framework.annotation.UrlMapping;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class Mapping {

    public static List<Class<?>> getUrlMappings(String basePackage) throws Exception {
        List<Class<?>> result = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(basePackage)
                .scan()) {

            ClassInfoList UrlMappingClasses = scanResult
                    .getClassesWithAnnotation(UrlMapping.class.getName());

            for (ClassInfo classInfo : UrlMappingClasses) {
                result.add(classInfo.loadClass());
            }
        }

        return result;
    }

    public static List<Class<?>> getUrlMappings() throws Exception {
        List<Class<?>> result = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {

            ClassInfoList UrlMappingClasses = scanResult
                    .getClassesWithAnnotation(UrlMapping.class.getName());

            for (ClassInfo classInfo : UrlMappingClasses) {
                result.add(classInfo.loadClass());
            }
        }

        return result;
    }
}
