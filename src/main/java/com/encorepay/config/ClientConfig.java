package com.encorepay.config;

public class ClientConfig {

    private final int    index;
    private final String name;
    private final String url;

    public ClientConfig(int index, String name, String url) {
        this.index = index;
        this.name  = name;
        this.url   = url;
    }

    public int    getIndex() { return index; }
    public String getName()  { return name;  }
    public String getUrl()   { return url;   }

    @Override
    public String toString() {
        return "ClientConfig{index=" + index + ", name='" + name + "', url='" + url + "'}";
    }
}
