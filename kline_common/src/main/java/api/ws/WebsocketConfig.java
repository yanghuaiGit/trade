package api.ws;

import api.ws.hertbeat.DefaultHeartBeatImpl;
import api.ws.hertbeat.HeartBeat;

import java.util.Map;

/**
 * @author luanxd
 * @date 2020-05-18
 */
public class WebsocketConfig {

    /**
     * websocket url-------------(如果存在则以url为主)
     */
    private String url;


    /**
     * websocket前缀
     */
    private String scheme = "ws";

    /**
     * 服务器IP
     */
    private String host;

    /**
     * 服务器端口
     */
    private int port;

    /**
     * 默认的websocket地址
     */
    private String path = "websocket";

    /**
     * url参数
     */
    private Map<String, Object> suffixParams;


    // Optional settings below
    /**
     * 检查
     */
    private Long checkLiveDuration;

    /**
     * 自动重启客户端
     */
    private Boolean autoRebootClient;

    /**
     * 保持连接状态
     */
    private Boolean keepAlive;

    /**
     * 读空闲超时时间
     */
    private Integer readerIdleTimeSeconds = 60;

    /**
     * 写空闲超时时间
     */
    private Integer writerIdleTimeSeconds = 60;

    /**
     * 所有空闲超时时间
     */
    private Integer allIdleTimeSeconds = 0;

    /**
     * 心跳实现
     */
    private HeartBeat heartBeat ;


    public boolean socksProxy = false;

    private String socksProxyHost = "localhost";


    private int socksProxyPort = 1081;


    public WebsocketConfig() {
    }

    public WebsocketConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public WebsocketConfig(String host, int port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
    }

    public WebsocketConfig(String scheme, String host, int port, String path) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
    }


    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getSuffixParams() {
        return suffixParams;
    }

    public void setSuffixParams(Map<String, Object> suffixParams) {
        this.suffixParams = suffixParams;
    }

    public Long getCheckLiveDuration() {
        return checkLiveDuration;
    }

    public void setCheckLiveDuration(Long checkLiveDuration) {
        this.checkLiveDuration = checkLiveDuration;
    }

    public Boolean getAutoRebootClient() {
        return autoRebootClient;
    }

    public void setAutoRebootClient(Boolean autoRebootClient) {
        this.autoRebootClient = autoRebootClient;
    }


    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Integer getReaderIdleTimeSeconds() {
        return readerIdleTimeSeconds;
    }

    public void setReaderIdleTimeSeconds(Integer readerIdleTimeSeconds) {
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
    }

    public Integer getWriterIdleTimeSeconds() {
        return writerIdleTimeSeconds;
    }

    public void setWriterIdleTimeSeconds(Integer writerIdleTimeSeconds) {
        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
    }

    public Integer getAllIdleTimeSeconds() {
        return allIdleTimeSeconds;
    }

    public void setAllIdleTimeSeconds(Integer allIdleTimeSeconds) {
        this.allIdleTimeSeconds = allIdleTimeSeconds;
    }

    public HeartBeat getHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(HeartBeat heartBeat) {
        this.heartBeat = heartBeat;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSocksProxyHost() {
        return socksProxyHost;
    }

    public void setSocksProxyHost(String socksProxyHost) {
        this.socksProxyHost = socksProxyHost;
    }

    public int getSocksProxyPort() {
        return socksProxyPort;
    }

    public void setSocksProxyPort(int socksProxyPort) {
        this.socksProxyPort = socksProxyPort;
    }

    public boolean isSocksProxy() {
        return socksProxy;
    }

    public void setSocksProxy(boolean socksProxy) {
        this.socksProxy = socksProxy;
    }
}
