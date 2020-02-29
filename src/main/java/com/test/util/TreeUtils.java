package com.test.util;

import com.test.annotation.SingleTreeNode;
import com.test.pojo.Area;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 费世程
 * @date 2020/2/29 15:37
 */
public class TreeUtils {

  private static Map<Class, Method> GET_CHILD_METHOD_MAP;
  private static Map<Class, Method> GET_PARENT_METHOD_MAP;
  private static Map<Class, Method> GET_CHILDlIST_METHOD_MAP;
  private static Map<Class, Method> SET_CHILDlIST_METHOD_MAP;

  static {
    ConcurrentHashMap<Class, Method> getChildMethodMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Class, Method> getParentMethodMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Class, Method> getChildListMethodList = new ConcurrentHashMap<>();
    ConcurrentHashMap<Class, Method> setChildListMethodMap = new ConcurrentHashMap<>();

    List<Class<?>> clazzList = ClassUtils.getAnnotatedClassOfPackages(Collections.singletonList("com.test.pojo"), SingleTreeNode.class);
    for (Class<?> clazz : clazzList) {
      SingleTreeNode annotation = clazz.getAnnotation(SingleTreeNode.class);
      String childId = annotation.childId();
      String parentId = annotation.parentId();
      String childList = annotation.childList();
      try {
        Method getChildId = clazz.getMethod("get" + upperTheFirstLetter(childId));
        getChildMethodMap.put(clazz, getChildId);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("获取<" + clazz.getName() + ">类<" + childId + ">的get方法出现异常：" + e.getMessage());
      }
      try {
        Method getParentId = clazz.getMethod("get" + upperTheFirstLetter(parentId));
        getParentMethodMap.put(clazz, getParentId);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("获取<" + clazz.getName() + ">类<" + parentId + ">的get方法出现异常：" + e.getMessage());
      }
      try {
        Method getChildList = clazz.getMethod("get" + upperTheFirstLetter(childList));
        getChildListMethodList.put(clazz, getChildList);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("获取<" + clazz.getName() + ">类<" + childList + ">的get方法出现异常：" + e.getMessage());
      }
      try {
        Method setChildList = clazz.getMethod("set" + upperTheFirstLetter(childList),List.class);
        setChildListMethodMap.put(clazz, setChildList);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("获取<" + clazz.getName() + ">类<" + childList + ">的set方法出现异常：" + e.getMessage());
      }
      GET_CHILD_METHOD_MAP = Collections.unmodifiableMap(getChildMethodMap);
      GET_PARENT_METHOD_MAP = Collections.unmodifiableMap(getParentMethodMap);
      GET_CHILDlIST_METHOD_MAP = Collections.unmodifiableMap(getChildListMethodList);
      SET_CHILDlIST_METHOD_MAP = Collections.unmodifiableMap(setChildListMethodMap);
    }
  }

  /**
   * 返回多根节点的list树结构
   *
   * @param sourceList 源list
   * @param clazz      数据源的class类型
   * @return java.util.List<T> 树结构的list列表
   * @author 费世程
   * @date 2020/2/29 22:50
   */
  public static <T> List<T> getObjectTree(List<T> sourceList, @NotNull Class<T> clazz) throws InvocationTargetException, IllegalAccessException {
    if (sourceList==null || sourceList.size()<2){
      return sourceList;
    }
    SingleTreeNode annotation=clazz.getAnnotation(SingleTreeNode.class);
    if (annotation==null){
      throw new RuntimeException("未找到<"+clazz+">类的SingleTreeNode注解！");
    }
    Method getChildIdMethod=GET_CHILD_METHOD_MAP.get(clazz);
    Method getParentIdMethod=GET_PARENT_METHOD_MAP.get(clazz);
    Method getChildListMethod=GET_CHILDlIST_METHOD_MAP.get(clazz);
    Method setChildListMethod=SET_CHILDlIST_METHOD_MAP.get(clazz);
    //初始化容器
    List<T> resList=new ArrayList<>();
    //将源转化成 areaId -> this 的Map
    Map<Object,T> sourceMap=new HashMap<>();
    for (T t:sourceList){
      Object childParam=getChildIdMethod.invoke(t);
      sourceMap.put(childParam,t);
    }
    for (T t:sourceList){
      Object parentBean=sourceMap.get(getParentIdMethod.invoke(t));
      if (parentBean==null){
        resList.add(t);
      }else{
        List<T> childList=(List<T>) getChildListMethod.invoke(parentBean);
        if (childList==null){
          childList=new ArrayList<>();
          setChildListMethod.invoke(parentBean,childList);
        }
        childList.add(t);
      }
    }
    return resList;
  }

  /**
   * 字符串首字母大写
   *
   * @param source 字符串
   * @return 新字符串
   */
  private static String upperTheFirstLetter(String source) {
    return source.substring(0, 1).toUpperCase().concat(source.substring(1));
  }

  public static void main(String[] args) {
    Area area1=new Area(1,"A",0);
    Area area2=new Area(2,"B",0);
    Area area3=new Area(3,"A-1",1);
    Area area4=new Area(4,"A-1-1",3);
    Area area5=new Area(5,"A-2",1);
    Area area6=new Area(6,"A-3",1);
    Area area7=new Area(7,"C",0);
    Area area8=new Area(8,"B-1",2);
    List<Area> sourceList=Arrays.asList(area1,area2,area3,area4,area5,area6,area7,area8);
    try {
      List<Area> resList=getObjectTree(sourceList,Area.class);
      System.out.println(resList);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

}
