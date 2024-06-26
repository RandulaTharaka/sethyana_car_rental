package com.sethyanacarrental.controller;


import com.sethyanacarrental.model.Employee;
import com.sethyanacarrental.model.User;
import com.sethyanacarrental.repository.EmployeeRepository;
import com.sethyanacarrental.repository.EmployeeStatusRepository;
import com.sethyanacarrental.repository.UserRepository;
import com.sethyanacarrental.service.EmailService;
import com.sethyanacarrental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;

@EnableAsync
@RequestMapping(value = "/employee")
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeRepository dao;

    @Autowired
    private UserRepository daouser;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PrivilegeController PrevilageController;

    @Autowired
    private EmployeeStatusRepository daoEmployeestatus;

    @GetMapping(value = "/listbypdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ModelAndView listbypdf() {
        List<Employee> cities = dao.findAll();
        return new ModelAndView("test.html", "Employee", cities);
    }

    @GetMapping(value = "/list", produces = "application/json")
    public List<Employee> list() {
        return dao.list();
    }

    @GetMapping(value = "/list/getemp", produces = "application/json")
    public Employee getnext() {
        LocalDate date = LocalDate.now();
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 7);

        // apply weekOfMonth()
        TemporalField weekOfMonth = weekFields.weekOfMonth();

        // get week of month for localdate
        Integer wom = date.get(weekOfMonth);

        // print results
        String year = ((Integer) date.getYear()).toString().substring(2);
        Integer month = date.getMonthValue();
        String monthofyear = "";
        String numberofmonth = "0" + wom;
        if (10 > month) {
            monthofyear = "0" + month;
        } else {
            monthofyear = month.toString();
        }

        Employee employee = new Employee();
        employee.setCallingname("BN" + year + monthofyear + numberofmonth);

        return employee;
    }

    @GetMapping(value = "/nextNumber", produces = "application/json")
    public Employee nextNumber() {
        String nextNumber = dao.getNextNumber();
        Employee nextEmpNumber = new Employee(nextNumber);
        return nextEmpNumber;
    }

    @GetMapping(value = "/list/withoutusers", produces = "application/json")
    public List<Employee> listwithoutusers() {
        return dao.listWithoutUsers();
    }

    @GetMapping(value = "/lists", produces = "application/json")
    public List<Employee> lists() {
        String name = "Admin";
        return dao.lists(name);
    }

    @GetMapping(value = "/list/withuseraccount", produces = "application/json")
    public List<Employee> listwithuseraccount() {
        return dao.listWithUseraccount();
    }


    @GetMapping(value = "/findAll", params = {"page", "size"}, produces = "application/json")
    public Page<Employee> findAll(@RequestParam("page") int page, @RequestParam("size") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        HashMap<String, Boolean> priv = PrevilageController.getPrivileges(user, "EMPLOYEE");
        System.out.println(priv);

        if (user != null && priv != null && priv.get("select"))
            return dao.findAll(PageRequest.of(page, size));
        else
            return null;
    }


    @GetMapping(value = "/findAll", params = {"page", "size", "searchtext"}, produces = "application/json")
    public Page<Employee> findAll(@RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("searchtext") String searchtext) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        HashMap<String, Boolean> priv = PrevilageController.getPrivileges(user, "EMPLOYEE");

        if (user != null && priv != null && priv.get("select")) {
            return dao.findAll(searchtext, PageRequest.of(page, size));
        }
        return null;
    }


    @PostMapping()
    public String add(@Validated @RequestBody Employee employee) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        HashMap<String, Boolean> priv = PrevilageController.getPrivileges(user, "EMPLOYEE");
        if (user != null && priv != null && priv.get("add")) {
            Employee empnic = dao.findByNIC(employee.getNic());
            Employee empnumber = dao.findByNumber(employee.getNumber());
            if (empnic != null)
                return "Error-Validation : NIC Exists";
            else if (empnumber != null)
                return "Error-Validation : Number Exists";
            else
                try {
                    dao.save(employee);
                    //  emailService.sendMail("iamrandula@gmail.com","Register Employee","Employee Registration Success Fully...!\n\n Thank You to join with us.. \n\n from : Sethyana Car Rentals");
                    return "0";
                } catch (Exception e) {
                    return "Error-Saving : " + e.getMessage();
                }
        }
        return "Error-Saving : You have no Permission";
    }


    @PutMapping()
    public String update(@Validated @RequestBody Employee employee) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        HashMap<String, Boolean> priv = PrevilageController.getPrivileges(user, "EMPLOYEE");

        if (user != null && priv != null && priv.get("update")) {
            Employee emp = dao.findByNIC(employee.getNic());
            if (emp == null || emp.getId() == employee.getId()) {
                try {
                    User empuser = daouser.getByEmploye(employee.getId());
                    if (empuser != null) {
                        if (employee.getEmployeestatusId().getName().equals("Resign") ||
                                employee.getEmployeestatusId().getName().equals("Deleted")) {
                            empuser.setActive(false);
                        } else if (employee.getEmployeestatusId().getName().equals("Working")) {
                            empuser.setActive(true);
                        }
                        daouser.save(empuser);
                    }
                    dao.save(employee);
                    return "0";
                } catch (Exception e) {
                    return "Error-Updating : " + e.getMessage();
                }
            } else {
                return "Error-Updating : NIC Exists";
            }
        }
        return "Error-Updating : You have no Permission";
    }

    @Transactional
    @DeleteMapping()
    public String delete(@RequestBody Employee employee) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        HashMap<String, Boolean> priv = PrevilageController.getPrivileges(user, "EMPLOYEE");

        if (user != null && priv != null && priv.get("delete")) {
            try {
                employee.setEmployeestatusId(daoEmployeestatus.getById(3));

                User empuser = daouser.getByEmploye(employee.getId());
                if (empuser != null) {
                    empuser.setActive(false);
                    daouser.save(empuser);
                }
                dao.save(employee);
                return "0";
            } catch (Exception e) {
                return "Error-Deleting : " + e.getMessage();
            }
        }
        return "Error-Deleting : You have no Permission";
    }
}
