package com.code.furry_takeout.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.furry_takeout.common.R;
import com.code.furry_takeout.entity.Employee;
import com.code.furry_takeout.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("employee")
public class EmployeeController {
    @Autowired
    EmployeeService employeeService;

    @PostMapping("login")
    public R<Employee> LoginMethod(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("用户：" + employee + " 尝试登录");

        // 从前端传递过来的 employee 中获取 password 对其 MD5 编码
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 根据 username 查询对应的用户
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, employee.getUsername());
        Employee employeeRes = employeeService.getOne(wrapper);

        if (employeeRes == null) {
            return R.error("用户不存在");
        } else if (!employeeRes.getPassword().equals(password)) {
            return R.error("密码错误");
        } else if (employeeRes.getStatus() == 0) {
            return R.error("账号被禁用");
        } else {
            request.getSession().setAttribute("employee", employeeRes.getId());
            log.info("用户 {} 登录成功", employeeRes.getId());
            return R.success(employeeRes);
        }
    }

    @PostMapping("logout")
    public R<String> LogoutMethod(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> AddEmployee(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("用户信息", employee);

        // 1. 创建用户，默认密码为 123456
        employee.setPassword("123456");
        employee.setPassword(DigestUtils.md5DigestAsHex(employee.getPassword().getBytes()));
        employee.setStatus(1);

        // 2. 记录操作者信息
        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        // 3. 保存用户信息
        employeeService.save(employee);
        log.info("添加员工成功");
        return R.success("添加员工成功");
    }

    // 姓名模糊搜索，用户列表，按照创建时间倒序排列，分页默认支持 10 条数据
    @GetMapping("page")
    public R<Page> PaginationEmployee(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(defaultValue = "") String name) {
        log.info("Employee 分页查询 " + "页" + page + " 页长" + pageSize + " 搜索词" + name);
        // 1. 构造分页器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        // 2. 设置查询条件
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(name), Employee::getName, name)
                .orderByDesc(Employee::getCreateTime);
        // 3. 分页查询
        employeeService.page(pageInfo, wrapper);
        // 4. 构造返回结果
        return R.success(pageInfo);
    }

    @GetMapping("/{id}")
    public R<Employee> GetEmployeeById(@PathVariable("id") Long id) {
//        Employee employee = employeeService.getById(id);

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getId, id);
        Employee employee = employeeService.getOne(wrapper);
        log.info("根据 ID 查询员工" + employee);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("系统中没有此员工");
    }

    @PutMapping()
    public R<String> ModifyEmployeeStatus(HttpServletRequest request, @RequestBody Employee employee) {
        // 1. 获取操作者信息
        Long ModifyBy = (Long) request.getSession().getAttribute("employee");

        // 2. 设置操作者信息
        employee.setUpdateUser(ModifyBy);

        // 3. 设置操作时间
        employee.setUpdateTime(LocalDateTime.now());
        log.info("更新员工信息成功：" + employee);

        // 4. 保存用户信息
        employeeService.updateById(employee);
//        employeeService.save(employee);

        return R.success("更改员工账号信息成功");
    }
}
