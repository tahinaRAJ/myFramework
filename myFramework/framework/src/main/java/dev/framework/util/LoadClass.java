package dev.framework.util;

import dev.framework.annotation.Controller;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.util.ArrayList;
import java.util.List;

public class LoadClass {

    public static List<Class<?>> getControllers(String basePackage) throws Exception {
        List<Class<?>> result = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(basePackage)
                .scan()) {

            ClassInfoList controllerClasses = scanResult
                    .getClassesWithAnnotation(Controller.class.getName());

            for (ClassInfo classInfo : controllerClasses) {
                result.add(classInfo.loadClass());
            }
        }

        return result;
    }

    public static List<Class<?>> getControllers() throws Exception {
        List<Class<?>> result = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {

            ClassInfoList controllerClasses = scanResult
                    .getClassesWithAnnotation(Controller.class.getName());

            for (ClassInfo classInfo : controllerClasses) {
                result.add(classInfo.loadClass());
            }
        }

        return result;
    }
}