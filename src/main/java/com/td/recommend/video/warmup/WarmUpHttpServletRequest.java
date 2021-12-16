package com.td.recommend.video.warmup;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

/**
 * Created by admin on 2017/8/18.
 */
public class WarmUpHttpServletRequest implements HttpServletRequest {
    private Map<String, String> paramMap = new HashMap<>();

    //We only use getParamter, just only implements this

    public WarmUpHttpServletRequest(String accessURL) {
        String[] fields = accessURL.split("\\?");
        if (fields.length < 2) {
            return;
        }

        String params = fields[1];
        String[] paramArray = params.split("&");
        for (String param : paramArray) {
            String[] paramVaule = param.split("=");
            if (paramVaule.length >= 2) {
                paramMap.put(paramVaule[0], paramVaule[1]);
            }
        }
    }

    @Override
    public String getParameter(String s) {
        return paramMap.get(s);
    }

    public void setParamMap(String key, String value){
        paramMap.put(key, value);
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Enumeration getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }



    @Override
    public Enumeration getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    @Override
    public Map getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public String changeSessionId(){ return null;}

    @Override
    public boolean authenticate(HttpServletResponse var1) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String var1, String var2) throws ServletException {

    }
    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String var1) throws IOException, ServletException {
        return null;
    }

//    @Override
//    public HttpUpgradeHandler upgrade(HttpUpgradeHandler var1) throws IOException, ServletException {
//        return null;
//    }
    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }


    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest var1, ServletResponse var2) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() { return false;}

    @Override
    public boolean isAsyncSupported() { return false;}

    @Override
    public AsyncContext getAsyncContext() { return null;}

    @Override
    public DispatcherType getDispatcherType() {return null;}

    public static void main(String[] args) {
        String accessURL = "/usernews/recommend?screenHeight=960&appId=A0008&serialId=fa97652c0ef84a28bb94f4c48b2899bc&chanId=nearme&ssl=1&lng=110.371072&screenDensity=1.5&bSsid=b4%3A15%3A13%3Aa2%3A53%3A7c&ssid=CMCC-502&coordType=b&deviceVersion=OPPO+A33m&verCode=3135&uhid=ttnsvoniovoab8702b64ac1c68fea8fc&lat=21.268475&deviceVendor=OPPO&limit=8&template=100_101_102_103_104_105_106_107_108_109_110_111_112_113_114_115_116&os=android&osVersion=5.1.1&netModel=w&imei=861260034606879&sign=676e22de47059082329d60221e2a668e&screenWidth=540&pageNo=7&verName=4.2.15&channel=1&openId=78e6a7505da24c9887d45757878db7ac&alg=gbdt";
        WarmUpHttpServletRequest request = new WarmUpHttpServletRequest(accessURL);
        String openId = request.getParameter("openId");
        System.out.println(openId);
        System.out.println(request.getParameter("lng"));
    }
}
