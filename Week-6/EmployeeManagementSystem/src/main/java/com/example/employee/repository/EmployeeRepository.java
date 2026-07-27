package com.example.employee.repository;

import com.example.employee.dto.EmployeeDto;
import com.example.employee.model.Employee;
import com.example.employee.projection.EmployeeSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Derived Query Method
    List<Employee> findByNameContaining(String name);

    // Derived Query Method with Pagination & Sorting
    Page<Employee> findByNameContaining(String name, Pageable pageable);

    // Custom JPQL Query
    @Query("SELECT e FROM Employee e WHERE e.department.name = :deptName")
    List<Employee> findEmployeesByDepartmentName(@Param("deptName") String deptName);

    // Named Query mapping to Employee.findByEmailNamed (declared in Employee entity)
    Employee findByEmailNamed(@Param("email") String email);

    // Interface-based Projection list
    @Query("SELECT e.name as name, e.email as email FROM Employee e WHERE e.department.id = :deptId")
    List<EmployeeSummary> findSummariesByDepartmentId(@Param("deptId") Long deptId);

    // Class-based Projection list using constructor expression
    @Query("SELECT new com.example.employee.dto.EmployeeDto(e.name, e.email, e.department.name) " +
           "FROM Employee e WHERE e.department.id = :deptId")
    List<EmployeeDto> findDtosByDepartmentId(@Param("deptId") Long deptId);
}
