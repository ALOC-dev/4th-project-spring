package dev.aloc.spring;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathComponentScanner implements ComponentScanner {

    // 찾고자 하는 @Component 애너테이션의 완전 수식 이름(FQN)
    private static final String COMPONENT_ANNOTATION_FQN = "dev.aloc.spring.annotation.Component";

    @Override
    public Set<Class<?>> scan(String basePackage) {
        Objects.requireNonNull(basePackage, "basePackage must not be null");
        // 리소스 경로로 치환
        String packagePath = basePackage.replace('.', '/');

        // 결과를 담을 Set (중복 방지 및 순서 무관)
        Set<Class<?>> components = new HashSet<>();

        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) cl = ClasspathComponentScanner.class.getClassLoader();
            // 동일 패키지가 여러 위치에 있을 수 있으므로, Enumeration으로 모두 조회
            Enumeration<URL> resources = cl.getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                // 파일 시스템(directory) or JAR
                // 그 외 범위는 필요 시 확장 가능
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    // 파일 시스템(directory) 형태 -> 재귀적으로 .class 탐색
                    File dir = toFile(url);
                    if (dir.exists() && dir.isDirectory()) {
                        scanDirectory(cl, basePackage, dir, components);
                    }
                }
                else if ("jar".equals(protocol)) {
                    // JAR 형태 -> 내부 엔트리 훑어보기
                    scanJar(cl, url, packagePath, components);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("패키지 스캔 중 오류 발생: " + basePackage, e);
        }

        return components;
    }

    // 파일 시스템(directory) 재귀적으로 훑으며 .class 파일 찾기
    private void scanDirectory(ClassLoader cl, String basePackage, File dir, Set<Class<?>> out) {
        File[] files = dir.listFiles();
        // 접근 권한 or I/O 문제
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                // 하위 패키지 재귀 탐색 (basePackage + "." + directory 이름)
                scanDirectory(cl, basePackage + "." + f.getName(), f, out);
            }
            else if (f.getName().endsWith(".class") && !f.getName().contains("$")) {
                String className = basePackage + "." + f.getName().substring(0, f.getName().length() - 6);
                tryLoadAndFilter(cl, className, out);
            }
        }
    }

    // JAR 파일 내부 엔트리 순회하며 .class 파일 찾기
    private void scanJar(ClassLoader cl, URL url, String packagePath, Set<Class<?>> out) {
        try {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = conn.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    
                    // 내부/익명/지역 클래스 제외 ($ 포함 경로)
                    if (name.startsWith(packagePath) && name.endsWith(".class") && !name.contains("$")) {
                        String className = name.substring(0, name.length() - 6).replace('/', '.');
                        tryLoadAndFilter(cl, className, out);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("JAR 스캔 중 오류 발생: " + url, e);
        }
    }

    // 클래스 이름으로 로딩 -> 구체 클래스 + @Component 존재 시, 결과에 추가
    private void tryLoadAndFilter(ClassLoader cl, String className, Set<Class<?>> out) {
        try {
            // false: 클래스 존재만 알고, 실행은 하지 않음
            Class<?> clazz = Class.forName(className, false, cl);
            if (isConcreteComponent(clazz)) {
                out.add(clazz);
            }
        }
        // Throwable > Exception
        // 전체 스캔이 멈추면 안 되니, 어떤 이유든 넘어가는 안전 장치
        catch (Throwable ignore) {
        }
    }

    // 구체 클래스이고, @Component 존재 여부 확인
    private boolean isConcreteComponent(Class<?> clazz) {
        // 인터페이스, 애너테이선, enum, 추상클래스 제외
        if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum()
            || Modifier.isAbstract(clazz.getModifiers())) return false;
        
        // @Component 존재 여부 확인
        return hasAnnotationByName(clazz, COMPONENT_ANNOTATION_FQN);
    }

    // @Component 존재 여부 확인 (FQN 문자열 비교)
    // 클래스 로더가 달라서 생기는 타입 비교 실패 방지
    private boolean hasAnnotationByName(Class<?> type, String annotationFqn) {
        for (Annotation ann : type.getAnnotations()) {
            if (ann.annotationType().getName().equals(annotationFqn)) return true;
        }
        return false;
    }

    // URL -> File 변환
    private File toFile(URL url) {
        try {
            return new File(url.toURI());
        }
        catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }
}