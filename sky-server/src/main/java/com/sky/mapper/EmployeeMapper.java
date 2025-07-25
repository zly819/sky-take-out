package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     * @param employee
     */
    @Insert("insert into employee values (null, #{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}," +
            " #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

    /**
     * 条件分页查询
     * @param name
     * @return
     */
    Page<Employee> list(String name);

    /**
     * 启用禁用员工，更新员工（包括更新状态）
     * @param employee
     */
    void update(Employee employee);


    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    Employee getById(Long id);
}
