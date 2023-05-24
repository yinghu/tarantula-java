package com.icodesoftware;

public interface AccessIndex extends Recoverable {

    int SYSTEM_INDEX = 0;

    int USER_INDEX = 1;

    int THIRD_PARTY_LOGIN_INDEX = 2;

    int DEVICE_LOGIN_INDEX = 3;

    int DEVELOPER_LOGIN_INDEX = 4;

    int referenceId();
}
