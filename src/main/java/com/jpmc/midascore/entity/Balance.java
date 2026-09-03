package com.jpmc.midascore.entity;

public class Balance {
    private float amount;

    public Balance(float amount){
        this.amount = amount;
    }

    public float getAmount(){
        return amount;
    }

    public void setAmount(float amount){
        this.amount = amount;
    }
}
