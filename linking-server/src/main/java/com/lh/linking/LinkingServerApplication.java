package com.lh.linking;

import com.lh.linking.service.SocketServer;
import com.lh.linking.util.PropertiesUtils;

public class LinkingServerApplication {

    public static void main(String[] args) {
        SocketServer server = new SocketServer();
        server.start(PropertiesUtils.getPropertyForInteger("server.port"));
    }
}
