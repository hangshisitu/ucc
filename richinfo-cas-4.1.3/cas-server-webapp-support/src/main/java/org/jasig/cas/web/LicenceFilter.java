package org.jasig.cas.web;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.servlet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Xiaoqiaojun on 2016/7/27.
 */
public class LicenceFilter implements Filter {

    private long expiretime = 0l;
    public void init(FilterConfig config) throws ServletException
    {
        //加载licence中的过期时间
        String liurl = config.getInitParameter("liurl");
        if(liurl==null || liurl.isEmpty())
        {
            throw new ServletException();
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(liurl);
        httpGet.addHeader("accept", "application/json");
        CloseableHttpResponse response1=null;
        try {
            response1 = httpclient.execute(httpGet);
            if(response1.getStatusLine().getStatusCode()!=200)
            {
                System.out.println("加载licence 失败");
                throw new ServletException();
            }
            HttpEntity entity1 = response1.getEntity();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((entity1.getContent())));
            String output;
            StringBuilder sb = new StringBuilder();
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            String error = jsonObject.getString("code");
            if(!error.equals("S_OK"))
            {
                System.out.println("加载licence 失败");
                throw new ServletException();
            }
            String strexprietime = jsonObject.getJSONObject("var").getJSONObject("licInfo").getString("expireTime");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            this.expiretime = df.parse(strexprietime).getTime();
            EntityUtils.consume(entity1);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            try{
                response1.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws  ServletException,IOException
    {

        if(this.expiretime==0l)
        {
            response.getOutputStream().println("License failed to load");
        }
        else if(this.expiretime<=new Date().getTime())
        {
            response.getOutputStream().println("License has expired");
            return;
        }
        chain.doFilter(request,response);
    }
    public void destroy()
    {
    }
}
