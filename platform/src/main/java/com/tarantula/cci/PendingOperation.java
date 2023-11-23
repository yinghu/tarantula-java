package com.tarantula.cci;

import com.icodesoftware.service.OnExchange;

public interface PendingOperation {

    void execute(OnExchange exchange) throws Exception;
}
