package com.example.employee.projection;

import org.springframework.beans.factory.annotation.Value;

public interface EmployeeSummary {

    String getName();

    String getEmail();

    // Interface-based projection with @Value using SpEL (Spring Expression Language)
    @Value("#{target.name + ' <' + target.email + '>'}")
    String getFormattedContact();
}
