package com.example.hearts;

public class PlayerReadyClass {
    private String UUID;
    private String name;  private Boolean ready;




    PlayerReadyClass(){}
    public PlayerReadyClass(String UUID, String name, Boolean ready) {
        this.UUID = UUID;
        this.name = name;
        this.ready = ready;
    }


    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }
}
