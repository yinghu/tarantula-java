package com.icodesoftware.protocol;

public interface UserSessionValidator {
    boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
}
