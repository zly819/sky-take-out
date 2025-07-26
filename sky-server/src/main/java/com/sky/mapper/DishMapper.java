package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.anno.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//@Mapper
public interface DishMapper {
    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)          //公共字段自动填充
//    @Options(useGeneratedKeys = true, keyProperty = "id")  //获取主键值，并且赋值给id属性
//    @Insert("insert into dish values (null, #{name}, #{categoryId},#{price}, #{image}," +
//            "#{description}, #{status},#{createTime}, #{updateTime},#{createUser}, #{updateUser})")
    void insert(Dish dish);

    /**
     * 分页查询菜品列表
     * @param dto
     * @return
     */
    Page<DishVO> list(DishPageQueryDTO dto);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    Dish selectById(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
