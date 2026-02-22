package com.clinicore.project.service;

public enum AccountRequestResultType {
    USER_ALREADY_EXISTS,   // user_profile already exists
    NEW,           // no existing request -> new PENDING created
    PENDING,       // existing PENDING -> you can still change name, role, etc
    APPROVED,       // already approved, admin must manage changes
    COMPLETED,      // request says COMPLETED (account done)
    REOPEN,      // EXPIRED -> reopened as PENDING
}
