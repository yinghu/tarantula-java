package com.tarantula.platform.configuration;

import com.icodesoftware.Recoverable;

public interface JDBCTask<T extends Recoverable> {
    boolean execute(T content);
}
