package com.djt.cbs.web.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.djt.cbs.system.entity.SysAccessLog;
import com.djt.cbs.system.entity.SysSession;
import com.djt.cbs.system.entity.SysUser;
import com.djt.cbs.system.service.SystemService;
import com.djt.common.ServiceResult;
import com.djt.common.util.ConvertUtil;
import com.djt.common.util.EncryptUtil;
import com.djt.common.util.StringUtil;

@Component
public class WebUtil implements ApplicationContextAware {
    private static org.apache.log4j.Logger log                               = org.apache.log4j.LogManager
                                                                                 .getLogger(WebUtil.class);

    public static final String             CBS_CLIENT_TOKEN_NAME             = "cbs-client-token";
    public static final String             CBS_AUTH_NAME                     = "cbs-auth";
    public static final String             CBS_USER_INFO_NAME                = "cbs-user";
    public static final String             CBS_USER_NAME                     = "cbs-user-name";

    public static final String             DEFAULT_PAGE_SIZE_NAME            = "paging-size";

    public static final String             CURRENT_USER_ID                   = "login-user-id";
    public static final String             CURRENT_USER_NAME                 = "login-user-name";

    public static final String             PASSWORD_MASK                     = "******";

    private static final int               PARAMETER_LOGING_VALUE_MAX_LENGTH = 500;
    private static final int               COOKIE_LOGING_VALUE_MAX_LENGTH    = 500;

    private static Set<String>             ignoredCookieLoggingEntries       = new HashSet<String>();
    private static ApplicationContext      context                           = null;

    static {
        ignoredCookieLoggingEntries.add("pgv_pvi");
        ignoredCookieLoggingEntries.add("pgv_si");
        ignoredCookieLoggingEntries.add("sgTrackerUserId");
        ignoredCookieLoggingEntries.add(CBS_CLIENT_TOKEN_NAME);
        ignoredCookieLoggingEntries.add(CBS_AUTH_NAME);
        ignoredCookieLoggingEntries.add(CBS_USER_INFO_NAME);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("WebUtil成功设置了ApplicationContext");
        context = applicationContext;
    }

    /**
     * 获取当前登陆用户的ID。
     * @param request
     * @return
     */
    public static int currentUserId(HttpServletRequest request) {
        //尝试从request属性获取
        if (request.getAttribute(CURRENT_USER_ID) != null) {
            int id = ConvertUtil.toInt(request.getAttribute(CURRENT_USER_ID), 0);
            if (id > 0) {
                if (log.isDebugEnabled())
                    log.debug("从request属性中获取到currentUserId：" + id);
                return id;
            }
        }
        //尝试从cookie读取
        String s = EncryptUtil.fromBASE64(readCookie(request, CBS_USER_INFO_NAME));
        if (StringUtil.isEmpty(s))
            return 0;
        String[] t = s.split(";");
        if (t.length != 2)
            return 0;
        int id = ConvertUtil.toInt(t[0], 0);
        if (log.isDebugEnabled())
            log.debug("从cookie中获取到currentUserId：" + id);
        return id;
    }

    /**
     * 获取当前登陆的用户名。
     * @param request
     * @return
     */
    public static String currentUserName(HttpServletRequest request) {
        //尝试从request属性获取
        if (request.getAttribute(CURRENT_USER_NAME) != null) {
            String name = String.valueOf(request.getAttribute(CURRENT_USER_NAME));
            if ("null".equalsIgnoreCase(name) || "undefined".equalsIgnoreCase(name))
                return "";
            return name;
        }
        //尝试从cookie读取
        String s = EncryptUtil.fromBASE64(readCookie(request, CBS_USER_INFO_NAME));
        if (StringUtil.isEmpty(s))
            return "";
        String[] t = s.split(";");
        if (t.length != 2)
            return "";
        if ("null".equalsIgnoreCase(t[1]) || "undefined".equalsIgnoreCase(t[1]))
            return "";
        return t[1];
    }

    /**
     * 取当前登陆认证的cookie值。
     * @param request
     * @return
     */
    public static String sessionId(HttpServletRequest request) {
        return readCookie(request, CBS_AUTH_NAME);
    }

    /**
     * 读取cookie值。
     * @param request
     * @param name
     * @return
     */
    public static String readCookie(HttpServletRequest request, String name) {
        String value = readCookie(request.getCookies(), name);
        if (!StringUtil.isEmpty(value))
            return value;
        return null;
    }

    private static String readCookie(Cookie[] cookies, String name) {
        if (cookies == null || cookies.length <= 0 || StringUtil.isEmpty(name))
            return null;
        for (Cookie c : cookies) {
            if (name.trim().equalsIgnoreCase(c.getName()))
                return c.getValue();
        }
        return null;
    }

    /**
     * 写cookie。
     * @param response
     * @param name cookie名称。
     * @param value cookie值。
     * @param age cookie有效时间，单位：秒。age&lt;0表示关闭浏览器失效；age=0表示立即失效（立即删除某个cookie）。
     */
    public static void writeCookie(HttpServletResponse response, String name, String value, int age) {
        if (response == null || StringUtil.isEmpty(name))
            return;
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(getDomain());
        cookie.setPath("/");
        cookie.setMaxAge(age);
        response.addCookie(cookie);
    }

    /**
     * 删除某个cookie。
     * @param response
     * @param name cookie名称。
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        writeCookie(response, name, null, 0);
    }

    /**
     * 检查用户是否已经登陆系统。
     * @return 已经登陆返回true，未登陆返回false。
     */
    public static boolean checkLogin(HttpServletRequest request) {
        //只检查cookie
        //菜单权限、操作权限均根据用户ID决定，即使用户伪造登陆认证cookie也无法访问没有权限的菜单和操作。
        String authToken = readCookie(request, CBS_AUTH_NAME);
        log.debug("cbs-auth:" + authToken);
        if (StringUtil.isEmpty(authToken, true))
            return false;

        int userId = currentUserId(request);
        return userId > 0;
    }

    /**
     * 访问权限校验。
     * @param request
     * @return
     */
    public static HttpJsonResult<Boolean> checkPermission(HttpServletRequest request) {
        HttpJsonResult<Boolean> result = new HttpJsonResult<Boolean>();
        result.setData(true);
        if (context == null) {
            log.warn("[sys][perm] context尚未初始化，忽略权限验证");
            return result;
        }
        SystemService service = context.getBean("systemService", SystemService.class);
        if (service == null) {
            log.warn("[sys][perm] 无法获取到SystemService，忽略权限验证");
            return result;
        }
        ServiceResult<SysSession> srvResult = service.checkPermissionByClientIp(
            WebUtil.sessionId(request), request.getRequestURI(), clientIp(request));
        //未通过权限验证
        if (!srvResult.getSuccess()) {
            result.setMessage(srvResult.getMessage());
            return result;
        }
        SysSession session = srvResult.getResult();
        if (session == null) {
            result.setMessage("您不能执行该操作，无效的会话状态，请确认您已经登陆系统");
            return result;
        }
        //通过权限验证，将登陆用户ID、用户名放入request中
        request.setAttribute(CURRENT_USER_ID, session.getUserId());
        request.setAttribute(CURRENT_USER_NAME, session.getUserName());
        return result;
    }

    /**
     * 登陆成功后写登陆认证cookie。
     * @param response
     * @param sessionId
     * @param user
     */
    public static void writeAuthCookie(HttpServletResponse response, String sessionId, SysUser user) {
        writeCookie(response, CBS_AUTH_NAME, sessionId, -1);
        writeCookie(response, CBS_USER_INFO_NAME,
            EncryptUtil.toBASE64(user.getUserId() + ";" + user.getUserName()), -1);
        writeCookie(response, CBS_USER_NAME, EncryptUtil.toBASE64(user.getUserName()), -1);
    }

    /**
     * 删除浏览器中的登陆认证cookie。
     * @param response
     */
    public static void deleteAuthCookie(HttpServletResponse response) {
        deleteCookie(response, CBS_AUTH_NAME);
        deleteCookie(response, CBS_USER_INFO_NAME);
    }

    /**
     * 获取客户端标识。
     * @param request {@link HttpServletRequest}对象，不允许为null。
     * @param response {@link HttpServletResponse}对象，可以为null值。
     * @return
     */
    public static String clientToken(HttpServletRequest request, HttpServletResponse response) {
        if (request == null)
            return "";

        String token = readCookie(request, CBS_CLIENT_TOKEN_NAME);
        if (!StringUtil.isEmpty(token))
            return token;

        if (response == null)
            return "";

        token = UUID.randomUUID().toString();
        writeCookie(response, CBS_CLIENT_TOKEN_NAME, token, 1 * 365 * 24 * 3600);
        return token;
    }

    /**
     * 获取站点域名
     * @return
     */
    private static String getDomain() {
        if (context == null)
            return "";
        return context.getBean("cookieDomain", java.lang.String.class);
    }

    /**
     * 获取客户端IP地址。
     * @param request
     * @return
     */
    public static String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        //如果客户端经过多级反向代理，则X-Forwarded-For的值并不止一个，而是一串IP值，
        //取X-Forwarded-For中第一个非unknown的有效IP字符串即为用户真实IP
        if (!StringUtil.isEmpty(ip) && ip.contains(",")) {
            String[] tokens = ip.split(",");
            for (String s : tokens) {
                if (StringUtil.isIp(s.trim())) {
                    ip = s.trim();
                    break;
                }
            }
        }
        if (StringUtil.isIp(ip))
            return ip;
        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtil.isIp(ip))
            return ip;
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtil.isIp(ip))
            return ip;
        return request.getRemoteAddr();
    }

    /**
     * 访问成功时记录访问日志。
     * @param request
     */
    public static void saveAccessLog(HttpServletRequest request) {
        saveAccessLog(request, 0, null, SysAccessLog.LOG_TYPE_ACCESS, true, null);
    }

    /**
     * 访问失败时记录访问日志。
     * @param request
     * @param errorMessage
     */
    public static void saveAccessLog(HttpServletRequest request, String errorMessage) {
        saveAccessLog(request, 0, null, SysAccessLog.LOG_TYPE_ACCESS, false, errorMessage);
    }

    /**
     * 记录登陆日志。
     * @param request
     * @param logType 日志类型。
     * @param userId 用户ID，如果值小于等于0，会尝试从会话状态中读取。
     * @param userName 用户名，如果为空，会尝试从会话状态中读取。
     * @param success 是否成功。
     * @param errorMessage 错误消息。
     */
    public static void saveLoginLog(HttpServletRequest request, int logType, int userId,
                                    String userName, boolean success, String errorMessage) {
        saveAccessLog(request, userId, userName, logType, success, errorMessage);
    }

    private static void saveAccessLog(HttpServletRequest request, int userId, String userName,
                                      int logType, boolean success, String errorMessage) {
        if (context == null) {
            log.info("[sys][log] context尚未初始化，无法记录日志");
            return;
        }
        try {
            SystemService service = context.getBean("systemService", SystemService.class);
            if (service == null) {
                log.info("[sys][log] 无法获取到SystemService，无法记录日志");
                return;
            }

            SysAccessLog accessLog = new SysAccessLog();
            accessLog.setLogType(logType);
            if (success)
                accessLog.setSuccess(1);
            else {
                accessLog.setSuccess(0);
                accessLog.setErrorMsg(errorMessage);
            }
            if (userId <= 0) {
                userId = currentUserId(request);
                userId = userId <= 0 ? 0 : userId;
            }
            if (StringUtil.isEmpty(userName)) {
                userName = currentUserName(request);
            }
            accessLog.setUserId(userId);
            accessLog.setUserName(userName);
            accessLog.setClientIp(clientIp(request));
            accessLog.setServerIp(request.getLocalAddr());
            accessLog.setRefererUrl(request.getHeader("referer"));
            accessLog.setVisitUrl(request.getRequestURI());
            accessLog.setClientToken(clientToken(request, null));
            accessLog.setSessionId(sessionId(request));
            accessLog.setAgent(request.getHeader("user-agent"));

            accessLog.setParamValue(parameterLoggingValue(request));
            accessLog.setCookieValue(cookieLoggingValue(request));

            service.createAccessLog(accessLog);
        } catch (Exception e) {
            log.warn("[sys][log] 无法记录访问日志", e);
        }
    }

    /**
     * 将request提交的所有cookie值拼接成字符串进行日志记录。
     * <p>
     * 1. 拼接后的格式：[name1:value1][name2:value2][name3:value3]...<br />
     * 2. 每个value如果超过一定长度（目前为30）会进行截断，并在最后补上"..."<br />
     * 3. 如果总长度超过限制（{@link WebUtil#COOKIE_LOGING_VALUE_MAX_LENGTH}），剩余的值会丢弃掉
     * @param request
     * @return
     */
    private static String cookieLoggingValue(HttpServletRequest request) {
        if (request == null)
            return "";
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0)
            return "";
        Map<String, String> entries = new HashMap<String, String>(cookies.length);
        for (Cookie c : cookies) {
            if (StringUtil.isEmpty(c.getValue()))
                continue;
            if (c.getName().startsWith("_") || c.getName().startsWith("Hm_lvt_")
                || c.getName().startsWith("Hm_lpvt_"))
                continue;
            if (ignoredCookieLoggingEntries.contains(c.getName()))
                continue;
            entries.put(c.getName(), c.getValue());
        }
        return join(entries, COOKIE_LOGING_VALUE_MAX_LENGTH, 50);
    }

    /**
     * 将request提交的所有参数（GET和FORM POST参数）拼接成字符串进行日志记录。
     * <p>
     * 1. 拼接后的格式：[name1:value1][name2:value2][name3:value3]...<br />
     * 2. 每个value如果超过一定长度（目前为30）会进行截断，并在最后补上"..."<br />
     * 3. 如果总长度超过限制（{@link WebUtil#PARAMETER_LOGING_VALUE_MAX_LENGTH}），剩余的参数会丢弃掉
     * @param request
     * @return
     */
    private static String parameterLoggingValue(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, String[]> params = request.getParameterMap();
        if (params == null)
            return "";
        Map<String, String> entries = new HashMap<String, String>(params.size());
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            //值为空不记录
            if (entry.getValue() == null || entry.getValue().length <= 0)
                continue;
            if ("password".equalsIgnoreCase(entry.getKey())) {
                entries.put("password", PASSWORD_MASK);
                continue;
            }
            if (entry.getValue().length == 1) {
                entries.put(entry.getKey(), entry.getValue()[0]);
                continue;
            }
            //获取参数值
            String s = "";
            for (String value : entry.getValue()) {
                if (s.length() > 0)
                    s = s + ",";
                s = s + value;
            }
            entries.put(entry.getKey(), s);
        }
        return join(entries, PARAMETER_LOGING_VALUE_MAX_LENGTH, 100);
    }

    private static String join(Map<String, String> values, int maxTotalLength, int maxEntryLength) {
        if (values == null || values.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        int length = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            //值为空时不记录
            if (StringUtil.isEmpty(entry.getValue()))
                continue;
            //剩余长度放不下这个参数
            if (entry.getKey().length() + 3 + length >= maxTotalLength)
                continue;
            String s = entry.getValue();
            //超过最大长度
            if (entry.getKey().length() + s.length() + length + 3 > maxTotalLength) {
                s = s.substring(0, maxTotalLength - entry.getKey().length() - length - 3);
                if (s.length() > maxEntryLength)
                    s = s.substring(0, maxEntryLength);
                if (s.length() > 6)
                    s = s.substring(0, s.length() - 3) + "...";
                sb.append('[').append(entry.getKey()).append(':').append(s).append(']');
                length = length + entry.getKey().length() + s.length() + 3;
                break;
            }
            //尚未超过最大长度
            if (s.length() > maxEntryLength)
                s = s.substring(0, maxEntryLength - 3) + "...";
            sb.append('[').append(entry.getKey()).append(':').append(s).append(']');
            length = length + entry.getKey().length() + s.length() + 3;
            //达到最大长度
            if (length >= maxTotalLength)
                break;
        }
        return sb.toString();
    }

    public static int computerPageSize(int screenHeight) {
        int defaultSize = 20, minSize = 10, rowHeight = 19;
        //top banner height: 37px, tab height: 25px, search form: 49px （用户管理界面，一行查询条件）, 
        //grid tbar: 27px, grid header: 23px, grid paging bar: 27px
        int rowAreaHeight = screenHeight - 37 - 25 - 49 - 27 - 22 - 27 - 10;
        if (rowAreaHeight <= 0)
            return defaultSize;
        int size = rowAreaHeight / rowHeight;
        return size < minSize ? defaultSize : size;
    }

    public static Object getBean(String beanName) {
        if (context == null)
            return null;
        return context.getBean(beanName);
    }

}