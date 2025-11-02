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

/**
 * classpath에서 @Component 어노테이션이 붙은 클래스를 모두 찾아 Set&lt;Class&lt;&#63;&gt;&gt;의 형태로 반환한다.
 */
public class ClasspathComponentScanner implements ComponentScanner {
    
    // 찾고자 하는 @Component 애너테이션의 완전 수식 이름(FQN)
    private static final String COMPONENT_ANNOTATION_FQN = "dev.aloc.spring.annotation.Component";
    
    /**
     * 인수로 전달받은 패키지 경로에서 @Component가 붙은 클래스를 찾아 Set 형태로 반환해준다.
     *
     * @param basePackage 탐색할 베이스 패키지
     * @return classpath 탐색으로 얻은 결과(&#64;Component가 붙은 클래스의 집합)를 반환한다.
     */
    @Override
    public Set<Class<?>> scan(String basePackage) {
        Objects.requireNonNull(basePackage, "basePackage must not be null");
        // 패키지 경로에서 치환한 리소스 경로
        String packagePath = basePackage.replace('.', '/');
        
        // 결과를 담을 Set (중복 방지 및 순서 무관)
        Set<Class<?>> components = new HashSet<>();
        
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClasspathComponentScanner.class.getClassLoader();
            }
            // 동일 패키지가 여러 위치에 있을 수 있으므로, Enumeration으로 모두 조회
            Enumeration<URL> resources = cl.getResources(packagePath);
            
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                // 파일 시스템(directory) or JAR. 그 외 범위는 필요 시 확장 가능
                String protocol = url.getProtocol();
                
                if ("file".equals(protocol)) {
                    // 파일 시스템(directory) 형태 -> 재귀적으로 .class 탐색
                    File dir = toFile(url);
                    if (dir.exists() && dir.isDirectory()) {
                        scanDirectory(cl, basePackage, dir, components);
                    }
                } else if ("jar".equals(protocol)) {
                    // JAR 형태 -> 내부 엔트리 훑어보기
                    scanJar(cl, url, packagePath, components);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("패키지 스캔 중 오류 발생: " + basePackage, e);
        }
        
        return components;
    }
    
    /**
     * 파일 시스템(directory)를 재귀적으로 훑으며 .class 파일을 찾아준다.
     *
     * @param cl          클래스 로더
     * @param basePackage 탐색중인 베이스 패키지
     * @param dir         스캔할 디렉토리
     * @param out         결과로 반환할 클래스 집합
     */
    private void scanDirectory(ClassLoader cl, String basePackage, File dir, Set<Class<?>> out) {
        File[] files = dir.listFiles();
        // 접근 권한 or I/O 문제
        if (files == null) {
            return;
        }
        
        for (File f : files) {
            if (f.isDirectory()) {
                // 하위 패키지 재귀 탐색 (basePackage + "." + directory 이름)
                scanDirectory(cl, basePackage + "." + f.getName(), f, out);
            } else if (f.getName().endsWith(".class") && !f.getName().contains("$")) {
                String className =
                    basePackage + "." + f.getName().substring(0, f.getName().length() - 6);
                tryLoadAndFilter(cl, className, out);
            }
        }
    }
    
    /**
     * JAR 파일 내부 엔트리를 순회하며 .class 파일을 찾는다.
     *
     * @param cl          클래스 로더
     * @param url         탐색할 JAR 파일의 URL
     * @param packagePath 현재 탐색 중인 패키지의 경로
     * @param out         결과로 반환할 클래스 집합
     */
    private void scanJar(ClassLoader cl, URL url, String packagePath, Set<Class<?>> out) {
        try {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = conn.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    
                    // 내부/익명/지역 클래스 제외 ($ 포함 경로)
                    if (name.startsWith(packagePath) && name.endsWith(".class") && !name.contains(
                        "$")) {
                        String className = name.substring(0, name.length() - 6).replace('/', '.');
                        tryLoadAndFilter(cl, className, out);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("JAR 스캔 중 오류 발생: " + url, e);
        }
    }
    
    /**
     * 클래스 이름으로 로드를 시도하여 구체클래스 + @Component 존재 시 결과에 추가한다.
     *
     * @param cl        클래스 로더
     * @param className Class&lt;&#63;&gt; 객체를 만들어줄 클래스의 이름
     * @param out       결과로 반환할 클래스 집합
     */
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
    
    /**
     * 구체 클래스일 때 @Component 어노테이션이 존재하는지 여부를 확인한다.
     *
     * @param clazz Component 어노테이션이 있는지 확인할 클래스.
     * @return 구체 클래스이면서 &#64;Component 어노테이션이 붙어있으면 true를 반환한다.
     */
    private boolean isConcreteComponent(Class<?> clazz) {
        // 인터페이스, 애너테이선, enum, 추상클래스 제외
        if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum()
            || Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        
        // @Component 존재 여부 확인
        return hasAnnotationByName(clazz);
    }
    
    /**
     * FQN 문자열 비교로 @Component 어노테이션의 존재 여부를 확인한다. 클래스 로더가 달라서 생기는 타입 비교 실패를 방지한다.
     * 메타 어노테이션(@Controller 등)도 체크한다.
     *
     * @param type 검사할 클래스
     * @return &#64;Component 어노테이션이 존재하면 true를 반환한다.
     */
    private boolean hasAnnotationByName(Class<?> type) {
        for (Annotation ann : type.getAnnotations()) {
            if (hasComponentAnnotation(ann.annotationType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 어노테이션 타입이 @Component를 가지고 있는지 재귀적으로 확인한다.
     *
     * @param annotationType 검사할 어노테이션 타입
     * @return @Component가 있으면 true
     */
    private boolean hasComponentAnnotation(Class<? extends Annotation> annotationType) {
        // 직접 @Component인지 확인
        if (annotationType.getName().equals(COMPONENT_ANNOTATION_FQN)) {
            return true;
        }

        // 메타 어노테이션 확인 (재귀적으로)
        for (Annotation metaAnn : annotationType.getAnnotations()) {
            Class<? extends Annotation> metaType = metaAnn.annotationType();
            // java.lang 패키지의 기본 어노테이션은 스킵
            if (metaType.getName().startsWith("java.lang.annotation.")) {
                continue;
            }
            if (hasComponentAnnotation(metaType)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * URL을 File로 변환한다.
     *
     * @param url 변환할 URL
     * @return 변환된 File 객체를 반환한다.
     */
    private File toFile(URL url) {
        try {
            return new File(url.toURI());
            
        } catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }
}