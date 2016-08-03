package com.ucc.addrbook;

import org.codehaus.jackson.map.ObjectMapper;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/2/23.
 */
public class AuthFilter implements Filter {
    String role="";
    Pattern pattern=null;
    Pattern pnormal[] = null;
    Pattern padmin []= null;
    Pattern proot []= null;
    Map<Pattern,String>normalpatterns;
    Map<Pattern,String>adminpatterns;
    Map<Pattern,String>rootpatterns;
    Map<String,Map<Pattern,Pattern>> patterns;
    ObjectMapper objectMapper = new ObjectMapper();
    public void destroy() {
    }

//    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
//        Map attrs = AssertionHolder.getAssertion().getPrincipal().getAttributes();
//        HttpServletRequest tmp = (HttpServletRequest) req;
//        String url = ((HttpServletRequest) req).getRequestURI();
//        String servletPath = ((HttpServletRequest) req).getServletPath();
//        String contextPath = ((HttpServletRequest) req).getContextPath();
//        String corpid = "";
//        Matcher matcher = pattern.matcher(url);
//        if(matcher.find()){
//            corpid = matcher.group(1);
//        }
//        if(attrs.get("employeeType")=="root")
//        {
////            chain.doFilter(req, resp);
//            ((HttpServletRequest)req).getRequestDispatcher(servletPath).forward(req,resp);
//        }
//        else if(attrs.get("employeeType")=="admin" &&
//                attrs.get("o") == corpid && role!="root")
//        {
////            chain.doFilter(req, resp);
//            ((HttpServletRequest)req).getRequestDispatcher(servletPath).forward(req,resp);
//        }
//        else if(attrs.get("o") == corpid &&
//                 role=="normal")
//        {
////            chain.doFilter(req, resp);
//            ((HttpServletRequest)req).getRequestDispatcher(servletPath).forward(req,resp);
//        }
////       else
////        {
////            //鉴权失败
////            ((HttpServletResponse)resp).setHeader("Content-Type","application/json");
////            Response ret = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
////            objectMapper.writeValue(((HttpServletResponse)resp).getOutputStream(),ret);
////        }
//    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        Map attrs = AssertionHolder.getAssertion().getPrincipal().getAttributes();
        HttpServletRequest tmp = (HttpServletRequest) req;
        String url = ((HttpServletRequest) req).getRequestURI();
        String method = ((HttpServletRequest) req).getMethod();
        String corpid = "";
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()){
            corpid = matcher.group(1);
        }
        for (Map.Entry<String,Map<Pattern,Pattern>> e:patterns.entrySet()
             ) {
            for (Map.Entry<Pattern, Pattern> ep :e.getValue().entrySet()
                    ) {
                if(ep.getValue().matcher(method).find() && ep.getKey().matcher(url).find()){
                    role=e.getKey();
                    break;
                }
            }
        }

        if(attrs.get("role").equals("root"))
        {
            chain.doFilter(req, resp);
//            ((HttpServletRequest)req).getRequestDispatcher(servletPath).forward(req,resp);
        }
        else if(attrs.get("role").equals("admin") &&
                attrs.get("corpId").equals(corpid) && !role.equals("root"))
        {
            chain.doFilter(req, resp);
//            ((HttpServletRequest)req).getRequestDispatcher(servletPath).forward(req,resp);
        }
        else if(role=="normal")
        {
            if(corpid.isEmpty())
            {
                chain.doFilter(req,resp);
            }else if(attrs.get("corpId").equals(corpid))
            {
                chain.doFilter(req,resp);
            }
//            ((HttpServletRequest)req).getRequestDispatcher(servletPath).forward(req,resp);
        }
       else
        {
            //鉴权失败
            ((HttpServletResponse)resp).setHeader("Content-Type","application/json");
            Response ret = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
            objectMapper.writeValue(((HttpServletResponse)resp).getOutputStream(),ret);
        }
    }

//    public void init(FilterConfig config) throws ServletException {
//        this.role = config.getInitParameter("role");
//        this.pattern = Pattern.compile(config.getInitParameter("regcorpid"));
//    }
    public void init(FilterConfig config) throws ServletException {
        this.pattern = Pattern.compile(config.getInitParameter("regcorpid"));
        this.patterns = new HashMap<String, Map<Pattern, Pattern>>();
        String rols[] = {"normal","admin","root"};
        for (int i=0;i<rols.length;i++)
        {
            String tmps[] = config.getInitParameter(rols[i]).split(";");
            Map<Pattern,Pattern> tmp = new HashMap<Pattern, Pattern>();
            for (int j=0;j<tmps.length;j++)
            {
                String methodurl[] = tmps[j].split(":");
                tmp.put(Pattern.compile(methodurl[0]),Pattern.compile(methodurl[1]));
            }
            this.patterns.put(rols[i],tmp);
        }
    }
}
