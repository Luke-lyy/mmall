package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int resultCount=categoryMapper.insertSelective(category);
         if (resultCount>0){
             return ServerResponse.createBySuccessMessage("添加品类成功");
         }
         return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse  updateCategoryName(Integer categoryId,String categoryName){
        if (categoryId==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int resultCount=categoryMapper.updateByPrimaryKeySelective(category);
        if (resultCount>0){
            return ServerResponse.createBySuccessMessage("更新categoryName成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字错误");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        if (categoryId==null){
            return ServerResponse.createByErrorMessage("查询品类参数错误");
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse selectCategoryAndChildrenById(Integer categoryId){
         return null;
    }

    //递归算法，算出子节点
    private Set<Category> findChildCategory(Set<Category>categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);
        }

        return null;
    }
}
