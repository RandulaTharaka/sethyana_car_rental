package com.sethyanacarrental.controller;


import com.sethyanacarrental.model.Gender;
import com.sethyanacarrental.repository.GenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping(value = "/gender")
@RestController
public class GenderController {

    @Autowired
    private GenderRepository dao;

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    public List<Gender> gender() {
        return dao.list();
    }
}
