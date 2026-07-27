package com.example.employee;

import com.example.employee.model.Department;
import com.example.employee.model.Employee;
import com.example.employee.model.secondary.LogEntry;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.repository.secondary.LogEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        logEntryRepository.deleteAll();
    }

    @Test
    public void testDepartmentAndEmployeeCrud() throws Exception {
        // 1. Create Department
        Department dept = new Department("Engineering");
        String deptJson = objectMapper.writeValueAsString(dept);
        
        String createdDeptStr = mockMvc.perform(post("/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(deptJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Engineering")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        Department createdDept = objectMapper.readValue(createdDeptStr, Department.class);
        assertNotNull(createdDept.getId());

        // Verify Auditing
        assertNotNull(createdDept.getCreatedDate());
        assertEquals("admin", createdDept.getCreatedBy());

        // 2. Create Employee associated with the department
        Employee emp = new Employee("John Doe", "john.doe@example.com", null);
        String empJson = objectMapper.writeValueAsString(emp);

        String createdEmpStr = mockMvc.perform(post("/employees")
                .param("departmentId", String.valueOf(createdDept.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(empJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andReturn().getResponse().getContentAsString();

        Employee createdEmp = objectMapper.readValue(createdEmpStr, Employee.class);
        assertNotNull(createdEmp.getId());
        assertEquals("admin", createdEmp.getCreatedBy());

        // 3. Get Employee By ID
        mockMvc.perform(get("/employees/" + createdEmp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")));

        // 4. Update Employee
        createdEmp.setName("John Smith");
        String updateJson = objectMapper.writeValueAsString(createdEmp);

        mockMvc.perform(put("/employees/" + createdEmp.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Smith")));

        // 5. Delete Employee
        mockMvc.perform(delete("/employees/" + createdEmp.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/employees/" + createdEmp.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPaginationAndSorting() throws Exception {
        Department dept = departmentRepository.save(new Department("Finance"));
        employeeRepository.save(new Employee("Alice", "alice@example.com", dept));
        employeeRepository.save(new Employee("Charlie", "charlie@example.com", dept));
        employeeRepository.save(new Employee("Bob", "bob@example.com", dept));

        // Search with sorting by name asc
        mockMvc.perform(get("/employees/search")
                .param("name", "")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "name")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name", is("Alice")))
                .andExpect(jsonPath("$.content[1].name", is("Bob")))
                .andExpect(jsonPath("$.content[2].name", is("Charlie")));
    }

    @Test
    public void testProjectionsAndNamedQueries() throws Exception {
        Department dept = departmentRepository.save(new Department("Sales"));
        Employee emp = employeeRepository.save(new Employee("Jane Doe", "jane.doe@example.com", dept));

        // Test Named Query
        mockMvc.perform(get("/employees/by-email-named")
                .param("email", "jane.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")));

        // Test Interface-based Projection
        mockMvc.perform(get("/employees/projections/summary")
                .param("departmentId", String.valueOf(dept.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Jane Doe")))
                .andExpect(jsonPath("$[0].formattedContact", is("Jane Doe <jane.doe@example.com>")));

        // Test Class-based Projection DTO
        mockMvc.perform(get("/employees/projections/dto")
                .param("departmentId", String.valueOf(dept.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Jane Doe")))
                .andExpect(jsonPath("$[0].departmentName", is("Sales")));
    }

    @Test
    public void testSecondaryDataSourceConfig() {
        // Save to secondary datasource log database repository
        LogEntry log = new LogEntry("Application started successfully");
        logEntryRepository.save(log);
        
        assertNotNull(log.getId());
        
        List<LogEntry> logs = logEntryRepository.findAll();
        assertEquals(1, logs.size());
        assertEquals("Application started successfully", logs.get(0).getMessage());
    }

    @Test
    public void testBatchProcessing() {
        Department dept = departmentRepository.save(new Department("Operations"));
        List<Employee> employees = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            employees.add(new Employee("Emp " + i, "emp" + i + "@example.com", dept));
        }
        
        // Save in bulk to verify batch processing
        List<Employee> saved = employeeRepository.saveAll(employees);
        assertEquals(25, saved.size());
    }
}
