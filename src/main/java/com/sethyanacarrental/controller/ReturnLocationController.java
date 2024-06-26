package com.sethyanacarrental.controller;

import com.sethyanacarrental.model.ReturnLocation;
import com.sethyanacarrental.repository.ReturnLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/return_location")
public class ReturnLocationController {

    @Autowired
    private ReturnLocationRepository dao;

    @GetMapping(value = "/list", produces = "application/json")
    public List<ReturnLocation> pickUpLocationList() {
        return dao.findAll();
    }
}
