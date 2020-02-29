package com.test.pojo;

import com.alibaba.fastjson.JSON;
import com.test.annotation.SingleTreeNode;
import lombok.Data;

import java.util.List;

/**
 * @author 费世程
 * @date 2020/2/29 15:31
 */
@Data
@SingleTreeNode(childId = "areaId",parentId = "parentId",childList = "childList")
public class Area {

  public Area(Integer areaId,String areaName,Integer parentId){
    this.areaId=areaId;
    this.areaName=areaName;
    this.parentId=parentId;
  }

  private Integer areaId;

  private String areaName;

  private Integer parentId;

  private List<Area> childList;

  public String toString() {
      return JSON.toJSONString(this);
  }

}
