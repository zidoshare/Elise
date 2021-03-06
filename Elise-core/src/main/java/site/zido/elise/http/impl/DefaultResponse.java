package site.zido.elise.http.impl;

import site.zido.elise.E;
import site.zido.elise.http.Body;
import site.zido.elise.http.Cookie;
import site.zido.elise.http.Header;
import site.zido.elise.http.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 下载的响应对象
 *
 * @author zido
 */
public class DefaultResponse implements Response {
    private static final long serialVersionUID = 8652625484193923483L;
    private String url;
    private int statusCode = E.StatusCode.CODE_200;
    private String reasonPhrase;

    private Body body;

    private boolean downloadSuccess = true;

    private List<Header> headers;

    private List<Cookie> cookies;

    /**
     * Instantiates a new Default response.
     */
    public DefaultResponse() {
        this.headers = new ArrayList<>();
    }

    public DefaultResponse(Response response) {
        this.url = response.getUrl();
        this.statusCode = response.getStatusCode();
        this.reasonPhrase = response.getReasonPhrase();
        this.body = response.getBody();
        this.headers = response.getAllHeaders();
        this.cookies = response.getCookies();
    }

    /**
     * Fail default response.
     *
     * @return the default response
     */
    public static DefaultResponse fail(String url) {
        DefaultResponse response = new DefaultResponse();
        response.setUrl(url);
        response.setDownloadSuccess(false);
        response.setStatusCode(E.StatusCode.CODE_DOWNLOAD_ERROR);
        return response;
    }

    /**
     * get url of current page
     *
     * @return url of current page
     */
    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Sets url.
     *
     * @param url the url
     * @return the url
     */
    public DefaultResponse setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets status code.
     *
     * @param statusCode the status code
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Sets reason phrase.
     *
     * @param reasonPhrase the reason phrase
     */
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Sets headers.
     *
     * @param headers the headers
     */
    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    /**
     * Is download success boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean isDownloadSuccess() {
        return downloadSuccess;
    }

    /**
     * Sets download success.
     *
     * @param downloadSuccess the download success
     */
    public void setDownloadSuccess(boolean downloadSuccess) {
        this.downloadSuccess = downloadSuccess;
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    @Override
    public Body getBody() {
        return body;
    }

    /**
     * Sets body.
     *
     * @param body the body
     */
    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public List<Cookie> getCookies() {
        return this.cookies;
    }

    /**
     * Sets cookies.
     *
     * @param cookies the cookies
     */
    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    @Override
    public List<Header> getHeaders(String key) {
        return headers.stream().filter(header -> Objects.equals(header.getName(), key)).collect(Collectors.toList());
    }

    @Override
    public List<Header> getAllHeaders() {
        return headers;
    }

    /**
     * Sets header.
     *
     * @param header the header
     */
    public void setHeader(Header header) {
        headers.add(header);
    }
}
