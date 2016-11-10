package com.magerman.nrpc;

public enum NoteCategory {

    DATA("Data note"), DESIGN("Design note"), PROFILE("Profile document");

    private final String name;

    private NoteCategory(String name) {
        this.name = name;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    @Override
    public String toString() {
        return name;
    }
}
