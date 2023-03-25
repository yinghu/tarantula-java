package com.icodesoftware.protocol;

import com.icodesoftware.service.ServiceProvider;

public interface GameServiceProvider extends ServiceProvider {

    GameServiceProxy gameServiceProxy(short serviceId);
}
