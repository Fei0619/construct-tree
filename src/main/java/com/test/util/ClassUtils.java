package com.test.util;

import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 费世程
 * @date 2020/2/29 15:40
 */
@Slf4j
public class ClassUtils {

  /**
   * 获取某些包下被某注解注释的所有类
   *
   * @param packageNameList 包列表
   * @param clazz           注解
   * @return 类列表
   */
  public static List<Class<?>> getAnnotatedClassOfPackages(@NotNull List<String> packageNameList,
                                                           @NotNull Class<? extends Annotation> clazz) {
    List<Class<?>> annotatedClassList= new ArrayList<>();
    for (String packageName : packageNameList) {
      List<Class<?>> classList = getClassOfPackage(packageName);
      for (Class<?> item : classList) {
        Annotation annotation=item.getAnnotation(clazz);
        if (annotation!=null){
          annotatedClassList.add(item);
        }
      }
    }
    return annotatedClassList;
  }

  /**
   * 获取包下所有的class
   *
   * @param packageName 包名
   * @return 类列表
   */
  public static List<Class<?>> getClassOfPackage(String packageName) {
    List<Class<?>> classList = new ArrayList<>();
    //是否迭代
    boolean recusrsive = true;
    try {
      //获取包的路径
      String packageDirName = packageName.replace(".", "/");
      //定义一个枚举的集合，并进行循环处理
      Enumeration<URL> dirs;
      dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
      while (dirs.hasMoreElements()) {
        //获取下一个元素
        URL url = dirs.nextElement();
        //获取协议的名称
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
          //1.如果是以文件的形式保存在服务器上
          //获取包的物理路径
          String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
          findClassInPackageByFile(packageName, filePath, recusrsive, classList);
        } else if ("jar".equals(protocol)) {
          //2.如果是jar包
          //定义一个JarFile
          JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
          //从此jar包得到一个枚举类
          Enumeration<JarEntry> entries = jar.entries();
          while (entries.hasMoreElements()) {
            //获取jar里的一个实体，可以是目录或者是jar包里的一些其他文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
              name = name.substring(1);
            }
            if (name.startsWith(packageDirName)) {
              int index = name.lastIndexOf('/');
              if (index != -1) {
                //如果以“/"结尾，是一个包，获取包名
                packageName = name.substring(0, index).replace('/', '.');
                if (recusrsive) {
                  //如果可以迭代下去
                  getClassOfPackage(packageName);
                }
              } else {
                if (name.endsWith(".class") && !entry.isDirectory()) {
                  String className = name.substring(packageName.length() + 1, name.length() - 6);
                  classList.add(Class.forName(packageName + "." + className));
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.debug("获取 " + packageName + " 包下所有的类出现异常：" + e.getMessage());
    }
    return classList;
  }

  /**
   * 获取包下以文件形式保存的所有class
   *
   * @param packageName 包名
   * @param packagePath 包路径
   * @param recursive   是否迭代循环
   * @param classList   类列表
   */
  private static void findClassInPackageByFile(String packageName,
                                               String packagePath,
                                               final boolean recursive,
                                               List<Class<?>> classList) {
    //获取此包的目录，建立一个File
    File dir = new File(packagePath);
    //如果不存在或者也不是目录就直接返回
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    //如果存在，就获取包下的所有文件，包括目录
    //自定义过滤规则
    File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
    if (dirFiles == null || dirFiles.length <= 0) {
      return;
    }
    for (File file : dirFiles) {
      if (file.isDirectory()) {
        //如果是目录则继续扫描
        findClassInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classList);
      } else {
        //如果是java类文件，去掉后面的.class,只保留类名
        String fullName = file.getName();
        String className = fullName.substring(0, fullName.length() - 6);
        try {
          classList.add(Class.forName(packageName + "." + className));
        } catch (ClassNotFoundException e) {
          log.debug("获取包下以文件形式保存的所有class出现异常：" + e.getException());
        }
      }
    }
  }

  public static void main(String[] args) {
    List<Class<?>> list = getClassOfPackage("com.test");
    for (Class<?> clazz : list) {
      System.out.println(clazz.getName());
    }
  }

}
