package com.server.nameserver;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
class Node {

    private @Id @GeneratedValue Long id;
    private String name;
    private int value;

    Node() {}

    Node(String name, int value) {
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