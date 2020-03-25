package com.university.bank;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
class Customer {

    private @Id @GeneratedValue Long id;
    private String name;
    private int value;

    Customer() {}

    Customer(String name, int value) {
        this.name = name;
        this.value = value;
    }

    // int getValue() {
    //     return value;
    // }

    // void addValue(int value){
    //     this .value += value;
    // }

    // int returnValue(int value) {
    //     if (this.value - value < 0 ){
    //         return -1;
    //     } else {
    //         return this.value - value;
    //     }
    // }
}