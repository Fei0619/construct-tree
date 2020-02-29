package com.test.annotation;

import java.lang.annotation.*;

/**
 * 单个树节点注释
 *
 * @author 费世程
 * @date 2020/2/29 15:18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SingleTreeNode {

  /**
   * 子节点id
   */
  String childId();
  /**
   * 父节点id
   */
  String parentId();
  /**
   * 该节点的子节点列表字段值
   */
  String childList();

}
