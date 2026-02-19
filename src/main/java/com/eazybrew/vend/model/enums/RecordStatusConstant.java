package com.eazybrew.vend.model.enums;

public enum RecordStatusConstant {
    ACTIVE, INACTIVE, TERMINATED, DELETED , PENDING, SUSPENDED, LEAVE, PROBATION, INTERDICTION,EX_EMPLOYEE;

    public static RecordStatusConstant getRecordStatus(String status) {
        if (status == null) {
            return null;
        }
        for (RecordStatusConstant userStatus : RecordStatusConstant.values()) {
            if (userStatus.name().equalsIgnoreCase(status)) {
                return userStatus;
            }
        }
        return null;
    }


}

