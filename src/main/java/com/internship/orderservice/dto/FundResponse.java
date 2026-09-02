package com.internship.orderservice.dto;

public class FundResponse {

    private Long id;

    private String name;

    private Double nav;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Double getNav() {
        return nav;
    }

    public void setNav(Double nav) {
        this.nav = nav;
    }
}