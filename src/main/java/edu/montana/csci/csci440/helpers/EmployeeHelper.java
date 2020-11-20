package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EmployeeHelper {
    public static String makeEmployeeTree() { // Got it down to one query!!
        List<Employee> employeeList = Employee.all(); // Get all employees. 1 Query!

        return "<ul>" + makeTree(employeeList.get(0), employeeList) + "</ul>";
    }


    //When given a list of employees and an employee e this function will return all employees that report to employee e.
    private static List<Employee> getReportsTo(Employee e, List<Employee> eList){
        List<Employee> reportedList = new LinkedList<>();
       for(int i = 0; i<eList.size(); i++){
           if(e.getEmployeeId() == eList.get(i).getReportsTo()){
               reportedList.add(eList.get(i));
           }
       }
       return reportedList;
    }



    public static String makeTree(Employee employee, List<Employee> all) {
        String list = "<li><a href='/employees" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>";

        List<Employee> reports = getReportsTo(employee,all); //Implements the same function but in memory.
        for (Employee report : reports) {
            list += makeTree(report, all);
        }


        return list + "</ul></li>";
    }
}
