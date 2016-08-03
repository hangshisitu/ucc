<!DOCTYPE HTML>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <meta charset="utf-8">
	<meta http-equiv="Content-Language" content="zh-CN"/>
	<meta name="Keywords" content=""/>
	<meta name="Description" content=""/>
    <title>彩讯统一认证中心</title>

	<%--<spring:theme code="standard.custom.css.file" var="customCssFile" />--%>
	<%--<link rel="stylesheet" href="<c:url value="${customCssFile}" />" />--%>
	<link rel="icon" href="<c:url value="/favicon.ico" />" type="image/x-icon" />

    <meta name="renderer" content="webkit">
<style>
body,h1,ul,p,form{ margin:0; padding:0;}

body{ font-family:Arial, Helvetica, sans-serif; font-size:12px; background:#fff;  z-index:1;}
h1,h2,h3,input,button{font-size:100%}
input,button,img{margin:0;line-height:normal}
input{ background:none;}
ul{ list-style:none;}
a{ color:#0c5993; text-decoration:none;}
i{ display:inline-block;}
/* clear float */
.clearfix:after{visibility:hidden;display:block;font-size:0;content:" ";clear:both;height:0;}
.clearfix{zoom:1;}
.clearAll{overflow:hidden;_zoom:1;}
.fl{ float:left;}
.ta_r{ text-align:right;}

.wrap{ width:1000px; margin:0 auto;}
.header{ margin-top:90px; width:1000px; margin-left:auto; margin-right:auto; padding:0 0 0px 0; position:relative; z-index:2;}
#logo{ float:left;}
.logo-til{ float:left; height:32px; line-height:32px; border-left:1px solid #a9d0ce; padding:0 0 0 10px; color:#0b7671; font-size:16px; margin:8px 0 0 10px;}
.topLink{ float:right; color:#ccc; position: relative; top:35px;}
.topLink a{ color:#666; margin:0 15px;}
.login-error{ color: #F00; padding-bottom: 5px; line-height: 20px;}

.main{  height:450px; position:relative; z-index:2;}
.content{ width:996px; height:450px; margin:0 auto;}
.login{ float:right; margin:68px 15px 0 0;_margin:68px 10px 0 0; width:340px;  box-shadow:0 0 8px rgba(0,0,0,0.2);}
.login-verifyCode{ float:right; margin:15px 15px 0 0;_margin:11px 10px 0 0; width:340px; border-radius:5px; box-shadow:0 0 8px rgba(0,0,0,0.1);}
.loginIpt{ position:relative; z-index:1;border:1px solid #adadad; height:35px; background:#fff; box-shadow:inset 0 1px 1px rgba(0,0,0,0.2); border-radius:4px;}
.login-tabs{ overflow:hidden;width: 341px;}
.login-tabs a{display:block; height:48px;width: 340px; line-height:48px; text-align:center; color:#666; font-size:16px; font-weight:bold; text-decoration:none; border-bottom:1px solid #e9e2da;background:#fef4ea; border-radius:4px 4px 0 0; cursor:inherit}


.tab-contents{ padding:21px 32px 23px 32px;background: rgb(255, 255, 255);background: rgba(255, 255, 255, 0.95); border-radius:0 0 4px 4px;}
.loginIpt{ position:relative; z-index:1;border:1px solid #ccc; height:35px; background:#fff;}
.loginIpt-focus{ box-shadow:0 0 5px rgba(243,139,16,.5);}

.i-uid,.i-psw,.i-phone{ background-image:url(resource/se/images/sg_img/log_sprite.png); background-repeat:no-repeat;}
.i-uid{ background-position:0 0; width:14px; height:16px; position:absolute; top:9px; left:8px;}
.i-psw{ background-position:-24px 0;width:14px; height:16px; position:absolute; top:9px; left:8px;}
.i-phone{background-position:-48px 0;width:13px; height:18px; position:absolute; top:8px; left:8px;}

.login-txt{ border:none medium;font-family:verdana;ime-mode:disabled; width:243px; height:21px; line-height:21px; color:#bebebe; font-size:14px; position: absolute; top:0px; left:5px; padding:7px 0; outline:none;}
.login-txt:-webkit-autofill{ -webkit-box-shadow: 0 0 0px 1000px white inset;}
.login-ul li{ margin-bottom:15px;}
.form-label{ padding-left:21px;display:block; height:20px; line-height:17px;*line-height:20px;}
.login-cb{ margin-right:5px; vertical-align:-2px;}
.fgt,.rmb{ color:#666;}


.b_btn{display:block; height:34px; line-height:34px; color:#fff; font-weight:bold; text-align:center; text-decoration:none; font-size:14px;background: #f58e15;width:100%;}
.b_btn:hover{background: #f0870b;}

.enterdiv{ float:right; padding-top:7px;}
.footer{ width:950px; margin:40px auto 0; color:#999; text-align:center;position:relative; z-index:2;}

/*下拉框*/
.fake_slt{ position:relative; display:inline-block; font-size:0; white-space:nowrap; vertical-align:middle; *display:inline; *zoom:1; padding:1px;}
.slt_inner{position:relative; z-index:1001;}
.fake_slt_on{ border:1px solid #ccc; border-bottom:1px solid #fff; padding:0;background: #fff;}
.fake_slt_on .slt_inner{background:#fff; }
.fake_slt_txt{ cursor:pointer; display:inline-block; vertical-align:top; font-size:12px; padding:0 5px; line-height:20px;overflow:hidden; white-space:nowrap; text-overflow:ellipsis;}
.fake_slt_a{ display:inline-block; width:20px; height:20px; text-align: left;}
.fake_slt_a i{ vertical-align:top;display: block;
font: 0/0 "宋体";
border: 5px solid;
border-color: #717171 #ffffff #ffffff #ffffff;
width: 0px; margin:7px 0 0 4px;}
.open{ z-index:1;}
.open .fake_slt_menu{ display:block;}
.fake_slt_menu{ width:120px; position:absolute; top:17px; right:-1px; z-index:1000; float:left; display:none; overflow-y:auto;overflow-x:hidden;}

.uid-slt{ display:none; width:273px; position:absolute; top:100%; left:-1px; z-index:1000; float:left; overflow-y:auto;overflow-x:hidden;padding:4px 0; margin:2px 0 0; background:#fff;border:1px solid #ccc;}
.uid-slt li{ padding-bottom:0!important; line-height:22px;}
.uid-slt li a{ display:block; color:#323232; padding:0 20px; clear:both; line-height:22px; white-space:nowrap; font-size:12px;+zoom:1;}
.uid-slt li a:hover{ text-decoration:none; background:#f38c12; color:#fff;}

.popList{ padding:4px 0; margin:2px 0 0; background:#fff;border:1px solid #ccc;}
.popList li{ padding-bottom:0!important; line-height:22px;}
.popList li a{ display:block; color:#323232; padding:0 20px; clear:both; line-height:22px; white-space:nowrap; font-size:12px;+zoom:1;}
.popList li a:hover{ text-decoration:none; background:#f38c12; color:#fff;}

.ym{ float:right; padding:5px 8px 6px 8px;; border-left:1px solid #ccc; height:24px; line-height:24px; font-size:14px; font-weight:bold; background:#fff; border-radius:0 5px 5px 0; font-size:12px; font-family:"verdana"; color:#4c4c4c;}

.pswtip {
	position: absolute;
	z-index: -999;
	top: 2px;
	_top: 4px;
	left: 5px;
	color: #C3C3C3;
	font-size: 14px;
	line-height: 33px;
	visibility: visible;
	cursor: text;
	padding-left: 4px;
}
.login-pop {
	float: right;
	padding: 3px;
	background: url(resource/login/images/pop_bg.png) repeat;
	position: relative;
	top: 99px;
}
.login-pop-inner {
	background: #FFF;
	width: 350px;
	height: 84px;
	position: relative;
}
.alarm {
	float: left;
	margin: 20px 0 0 18px;
}
.alarm-font {
	overflow: hidden;
	zoom: 1;
	padding: 20px 0 0 10px;
}
.pop_direct {
	width: 19px;
	height: 32px;
	background: url(resource/login/images/pop_direct.png) repeat;
	position: absolute;
	top: 29px;
	right: -15px;
}
.loginSelected{
	text-decoration: none;
	background: #f38c12;
	color: #fff;
}
#imgRnd{ border:1px solid #ddd; margin-right:5px;}
.codeImg{ margin-bottom:10px;}
#liCodeBox {
    height: 37px;
}
.getCodeBtn {
    background: rgba(0, 0, 0, 0) linear-gradient(#fafafa 0px, #f4f4f4 100%) repeat scroll 0 0;
    border: 1px solid #d7d7d7;
    border-radius: 3px;
    color: #666;
    cursor: pointer;
    font-size: 14px;
    height: 37px;
    line-height: 35px;
    text-align: center;
    width: 110px;
}
.loginCode {
    background: #fff none repeat scroll 0 0;
    border: 1px solid #ccc;
    height: 35px;
    position: relative;
    z-index: 1;
}

.msg {
	padding: 5px;
	margin-bottom: 0px;
}

/*.msg.errors {*/
	/*border: 1px dotted #BB0000;*/
	/*color: #BB0000;*/
	/*padding-left: 100px;*/
	/*!*background: url(../images/error.gif) no-repeat 20px center;*!*/
/*}*/

.msg.success { border: 1px dotted #390; color: #390; padding-left: 100px; background: url(../images/success.png) no-repeat 20px center; }
.msg.info { border: 1px dotted #008; color: #008; padding-left: 100px; background: url(../images/info.png) no-repeat 20px center; }
.msg.question { border: 1px dotted #390; color: #390; padding-left: 100px; background: url(../images/question.png) no-repeat 20px center; }
.msg.warn { border: 1px dotted #960; color: #960; padding-left: 100px; background: #ffbc8f url(../images/info.png) no-repeat 20px center; }

.errors {
	color: #F00;
	padding-bottom: 0px;
	line-height: 20px;
}


/*noscript*/
.noscriptTips{ width:400px; height:150px;padding:50px 50px; margin:-200px -250px; left:50%; position:fixed; _position:absolute; top:50%; background:#fff; z-index:99; border:1px solid #ddd; border-radius:10px; box-shadow:5px 5px 5px #ccc;}
.noscriptTips p{ padding-left:80px; font-size:14px;line-height:24px; width:320px; position:relative;}
.noscriptTips em{ font-size:54px; display:inline-block;border:2px solid #ddd; border-radius:64px; width:64px; height:64px; text-align:center; line-height:64px; color:#E1B8B8; left:0px; top:0px; position:absolute; font-style:normal;}

</style>   
<script >
var gMain={
	  webPath:"/webmail",
	  homesetTitleAndCopy:'{"loginpage":{"copyright":{"showlabel":"首页底部版权信息","showtext":"Copyright©2011-2014 Thinkcloud.All Right Reserved"},"title":{"showlabel":"浏览器标题设置","showtext":"Thinkmail 4.3.1"},"title_admin":{"showlabel":"管理平台标题设置","showtext":"dddd"}}}',
	  homesetLinks:'{"系统管理-首页设置":"http://test2.com"}'
	}

var isEncryptPwd="0";
var cfgAuthMd5="0";
</script>
<!--<script type="text/javascript" charset="utf-8" src="http://192.168.34.51/resource/se/common/jquery.js?20130916"></script>-->
<script src="resource/se/common/jquery.js"></script>
<!--<script type="text/javascript" charset="utf-8" src="http://192.168.34.51/resource/se/common/security.js"></script>-->
<!--<script type="text/javascript" charset="utf-8" src="http://192.168.34.51/resource/se/common/com.js?20130916"></script>-->
<script src="resource/se/common/com.js"></script>

<script type="text/javascript" charset="utf-8" src="resource/login/js/lg.js?20130916"></script>
<script type="text/javascript" charset="utf-8">
   var skinControl = "0";//false
</script>
</head>
	<script type="text/javascript">
		try{
		    resettingTop();
		}catch(e){
		    try{
		        resettingTop();
		    }catch(e){}
		}
		function resettingTop(){
		    if (window.top != window) {
		        window.top.location.href = window.location.href + "?from=iframe";
		    }
		}

		$(function(){
			$("input").focus(function(){
				var id = "#tip_"+this.id;
				$(id).hide();
			});
			$("input").blur(function(){
				var id = "#tip_"+this.id;
				if(this.value=="")
				{
					$(id).show();
				}
			});
		});
		function changeautho()
		{
			$("#captchaimg").attr("src","captcha.jpg?"+Math.random());
		}
		function doLogin(){
		    Login.doSubmit();
		}
		var ssl = "0";
	    var constant = {
			 "imgUrl":"/webmail"+"/common/validatecode.do"
		};
		var domains = ["arsenal.com","bak-22.com","bak-22234.com","bak-3.com","bak-4.com","bak-5.com","bak-ccc.cn","bak-cccc.cn","bak-cook.com","bak-qq.com","bak-sd5.com","bak-sss.com","bak-sssd.com","books.com","chd.com.cn","chdhk.com.cn","chelsea.com","cool.com","cyc.com","gzqydl.cn","hhi.com.cn","mailmove.com","mu.com","premierleague.com","sac-china.com","sd.com","sdd2.com","sf1.com","sf2.com","sss.com","thinkmail.com","weikang.com"];
		var bgimgorder = "0";
		//var bgImageUrl = "resource/custom/images/mailtype_0/default/user.jpg";
		var power_mail= true;
		var power_contacts= true;
		var power_disk= true;
		var power_schedule= true;
		var regist = true;
		var webpath = "/webmail" || "/webmail";
		var linkList = '{"APP下载中心":"/webmail/se/account/download.do","ThinkMail官网":"http://www.thinkcloud.cn/","help_center":"","add_favorite":""}' || '{"app_download":"","Thinkmail官网":"http://www.thinkcloud.cn/","help_center":"","add_favorite":""}';
		var isShowMobile = '1';
	    var defaultDomain = "";
		

		
	</script>
<body>

<div class="header clearfix" id="header">
  <h1 id="logo"><img align="top" src="resource/custom/images/mailtype_0/default/logo_login2.jpg" alt="ThinkMail 企业邮箱"/></h1>
    <!--<p style="width:75%;text-align:right" id="loginLink" class="topLink"><a href="/webmail/extra/register.do?func=showForm">帮助中心</a>|<a style="margin-right: 18px;" href="javascript:AddFavorite();" id="addFavorite">加入收藏夹</a></p>-->
</div><!--header-->


<div class="main" id="main" style="background:#fff url('resource/custom/images/mailtype_0/default/bg_reg.jpg') no-repeat scroll center center;">
<!--<div class="main" id="main">-->
  <div class="content clearfix" id="mainContent2" style="background:#fff url('resource/custom/images/mailtype_0/default/bg_reg.jpg') no-repeat scroll center center;">
<!-- <div class="content clearfix" id="mainContent2">-->
	  <c:choose>
		  <c:when test="${sessionScope.errcount >=3}">
			  <div class="login login-verifyCode" id="login">
				  <div class="login-tabs clearfix">
					  <a id="userLogin" href="javascript:;" class="on">用户登录</a>

				  </div>
				  <div class="tab-contents" id="login_divxxx">
					  <div class="tab-content1" style="display:block;">
						  <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">
							  <ul class="login-ul" id="login_ul">

								  <form:errors path="*" id="msg_nm" cssClass="msg errors" element="div" htmlEscape="false" />
								  <li id="liUserNum">
									  <div class="loginIpt loginIpt-focus" id="userNumBoxxxxx" style="z-index:2;">
										  <c:choose>
											  <c:when test="${not empty sessionScope.openIdLocalId}">
												  <strong><c:out value="${sessionScope.openIdLocalId}" /></strong>
												  <input  id="username" name="username" value="<c:out value="${sessionScope.openIdLocalId}" />" autocomplete="off" style="font-weight: bold; color: #333;" tabIndex="1"   class="login-txt" type="text" />
											  </c:when>
											  <c:otherwise>
												  <input  autocomplete="off" style="font-weight: bold; color: #333;" tabIndex="1" id="username"  name="username" path="username" class="login-txt" type="text" />
											  </c:otherwise>
										  </c:choose>
										  <span class="pswtip" id="tip_username">邮箱/用户名/手机号</span>
										  <ul class="uid-slt" id="loginPannel" style="display:none;height: 212px;">
										  </ul>
									  </div>
								  </li>


									  <%--<form:errors path="password" id="msg_pd" cssClass="msg errors" element="div" htmlEscape="false" />--%>
								  <li id="pwdBox">
									  <div class="loginIpt">
										  <input id="password" style="font-weight: bold; color: #333;" name="password" path="password" tabIndex="2" class="login-txt" type="password" autocomplete="off" value=""/>
										  <span class="pswtip" id="tip_password">密码</span>
									  </div>
								  </li>

									  <%--<form:errors path="authcode" id="msg_ac" cssClass="msg errors" element="div" htmlEscape="false" />--%>

								  <!-- 验证码 -->
								  <li style="z-index:800;display:block; margin-bottom:5px;" id="li_verifyCode">
									  <c:choose>
										  <c:when test="${sessionScope.errcount >=3}">
											  <div class="loginIpt" id="pwdBoxxxxx" style="width: 173px;">
												  <input id="authcode" style="width: 173px; font-weight: bold; color: #333;left:10px;"  name="authcode"  path="authcode" tabIndex="3" class="login-txt" type="text" autocomplete="off" value="" />
												  <span class="pswtip" id="tip_authcode" style="left: 7px; ">图片验证码</span>
												  <img id="captchaimg" style="display:block; position: absolute; right: -100px;" alt="<spring:message code="required.authcode" />" onclick="this.src='captcha.jpg?'+Math.random()" width="95" height="35" src="captcha.jpg">
											  </div>
											  <%--<div style="padding-top:14px;" class="clear codeImg">--%>
											  <%--&lt;%&ndash;<img style="display:block; float:left;" onClick="Login.refreshImgRndCode();return false;" id="imgRnd" alt="点击更换" src="resource/se/images/yzm_img.jpg">&ndash;%&gt;--%>
											  <%--&lt;%&ndash;<img id="captchaimg" style="display:block; float:left;" alt="<spring:message code="required.authcode" />" onclick="this.src='captcha.jpg?'+Math.random()" width="95" height="45" src="captcha.jpg">&ndash;%&gt;--%>
											  <%--&lt;%&ndash;<p style="color:#666;display:block; margin-top:0; margin-left:176px; line-height:18px;">点击图片换一张<br>&ndash;%&gt;--%>
											  <%--&lt;%&ndash;<a href="javascript:void(0);" onclick="changeautho()" id="a_imageRndxxxxx" style="font-size:12px;margin-top:2px;">看不清,换一张</a>&ndash;%&gt;--%>
											  <%--&lt;%&ndash;</p>&ndash;%&gt;--%>
											  <%--</div>--%>
										  </c:when>
									  </c:choose>

								  </li>

								  <!--
						  <section class="row check">
						  <p>
						  <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
						  <label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
						  <br/>
						  <input id="publicWorkstation" name="publicWorkstation" value="false" tabindex="4" type="checkbox" />
						  <label for="publicWorkstation"><spring:message code="screen.welcome.label.publicstation" /></label>
						  <br/>
						  <input type="checkbox" name="rememberMe" id="rememberMe" value="true" tabindex="5"  />
						  <label for="rememberMe"><spring:message code="screen.rememberme.checkbox.title" /></label>
						  </p>
						  </section>
						  -->

								  <section class="row btn-row">
									  <input type="hidden" name="lt" value="${loginTicket}" />
									  <input type="hidden" name="execution" value="${flowExecutionKey}" />
									  <input type="hidden" name="_eventId" value="submit" />

									  <li class="clearfix" style="padding-top: 10px;">
											  <%--<a href="javascript:;" tabIndex="6" class="b_btn fl" id="login_otp">--%>
											  <%--<span id="b_text">登 录</span>--%>
											  <%--</a>--%>


										  <input class="b_btn fl" name="submit" accesskey="l" value="登 录" tabindex="6" type="submit" />
									  </li>
										  <%--<input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="7" type="reset" />--%>
								  </section>
							  </ul>
						  </form:form>

					  </div>
				  </div>
			  </div>
		  </c:when>
		  <c:otherwise>
			  <div class="login" id="login">
				  <div class="login-tabs clearfix">
					  <a id="userLogin" href="javascript:;" class="on">用户登录</a>

				  </div>
				  <div class="tab-contents" id="login_divxxx">
					  <div class="tab-content1" style="display:block;">
						  <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">
							  <ul class="login-ul" id="login_ul">

								  <form:errors path="*" id="msg_nm" cssClass="msg errors" element="div" htmlEscape="false" />
								  <li id="liUserNum">
									  <div class="loginIpt loginIpt-focus" id="userNumBoxxxxx" style="z-index:2;">
										  <c:choose>
											  <c:when test="${not empty sessionScope.openIdLocalId}">
												  <strong><c:out value="${sessionScope.openIdLocalId}" /></strong>
												  <input  id="username" name="username" value="<c:out value="${sessionScope.openIdLocalId}" />" autocomplete="off" style="font-weight: bold; color: #333;" tabIndex="1"   class="login-txt" type="text" />
											  </c:when>
											  <c:otherwise>
												  <input  autocomplete="off" style="font-weight: bold; color: #333;" tabIndex="1" id="username"  name="username" path="username" class="login-txt" type="text" />
											  </c:otherwise>
										  </c:choose>
										  <span class="pswtip" id="tip_username">邮箱/用户名/手机号</span>
										  <ul class="uid-slt" id="loginPannel" style="display:none;height: 212px;">
										  </ul>
									  </div>
								  </li>


									  <%--<form:errors path="password" id="msg_pd" cssClass="msg errors" element="div" htmlEscape="false" />--%>
								  <li id="pwdBox">
									  <div class="loginIpt">
										  <input id="password" style="font-weight: bold; color: #333;" name="password" path="password" tabIndex="2" class="login-txt" type="password" autocomplete="off" value=""/>
										  <span class="pswtip" id="tip_password">密码</span>
									  </div>
								  </li>

									  <%--<form:errors path="authcode" id="msg_ac" cssClass="msg errors" element="div" htmlEscape="false" />--%>

								  <!-- 验证码 -->
								  <li style="z-index:800;display:block; margin-bottom:5px;" id="li_verifyCode">
									  <c:choose>
										  <c:when test="${sessionScope.errcount >=3}">
											  <div class="loginIpt" id="pwdBoxxxxx" style="width: 173px;">
												  <input id="authcode" style="width: 173px; font-weight: bold; color: #333;left:10px;"  name="authcode"  path="authcode" tabIndex="3" class="login-txt" type="text" autocomplete="off" value="" />
												  <span class="pswtip" id="tip_authcode" style="left: 7px; ">图片验证码</span>
												  <img id="captchaimg" style="display:block; position: absolute; right: -100px;" alt="<spring:message code="required.authcode" />" onclick="this.src='captcha.jpg?'+Math.random()" width="95" height="35" src="captcha.jpg">
											  </div>
											  <%--<div style="padding-top:14px;" class="clear codeImg">--%>
											  <%--&lt;%&ndash;<img style="display:block; float:left;" onClick="Login.refreshImgRndCode();return false;" id="imgRnd" alt="点击更换" src="resource/se/images/yzm_img.jpg">&ndash;%&gt;--%>
											  <%--&lt;%&ndash;<img id="captchaimg" style="display:block; float:left;" alt="<spring:message code="required.authcode" />" onclick="this.src='captcha.jpg?'+Math.random()" width="95" height="45" src="captcha.jpg">&ndash;%&gt;--%>
											  <%--&lt;%&ndash;<p style="color:#666;display:block; margin-top:0; margin-left:176px; line-height:18px;">点击图片换一张<br>&ndash;%&gt;--%>
											  <%--&lt;%&ndash;<a href="javascript:void(0);" onclick="changeautho()" id="a_imageRndxxxxx" style="font-size:12px;margin-top:2px;">看不清,换一张</a>&ndash;%&gt;--%>
											  <%--&lt;%&ndash;</p>&ndash;%&gt;--%>
											  <%--</div>--%>
										  </c:when>
									  </c:choose>

								  </li>

								  <!--
						  <section class="row check">
						  <p>
						  <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
						  <label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
						  <br/>
						  <input id="publicWorkstation" name="publicWorkstation" value="false" tabindex="4" type="checkbox" />
						  <label for="publicWorkstation"><spring:message code="screen.welcome.label.publicstation" /></label>
						  <br/>
						  <input type="checkbox" name="rememberMe" id="rememberMe" value="true" tabindex="5"  />
						  <label for="rememberMe"><spring:message code="screen.rememberme.checkbox.title" /></label>
						  </p>
						  </section>
						  -->

								  <section class="row btn-row">
									  <input type="hidden" name="lt" value="${loginTicket}" />
									  <input type="hidden" name="execution" value="${flowExecutionKey}" />
									  <input type="hidden" name="_eventId" value="submit" />

									  <li class="clearfix" style="padding-top: 10px;">
											  <%--<a href="javascript:;" tabIndex="6" class="b_btn fl" id="login_otp">--%>
											  <%--<span id="b_text">登 录</span>--%>
											  <%--</a>--%>


										  <input class="b_btn fl" name="submit" accesskey="l" value="登 录" tabindex="6" type="submit" />
									  </li>
										  <%--<input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="7" type="reset" />--%>
								  </section>
							  </ul>
						  </form:form>

					  </div>
				  </div>
			  </div>
		  </c:otherwise>
	  </c:choose>

	
  </div>
</div>
<!--main-->

<div class="footer" id="footer">©2004-2018 版权所有  深圳市彩讯科技有限公司　京ICP备02558978号-4</div>


<!--勿删-->
<noscript>
<div class="noscriptTips">
  <p class="pr"><em>i</em>您的浏览器禁用了JavaScript脚本,这会影响您正常浏览网站.<br />
    请注意浏览器周边提示,启用JavaScript,您将有更好的浏览体验.<br />
    选择&quot;允许阻止的内容&quot;,允许ActiveX脚本运行.</p>
</div>
</noscript>
<!--勿删-->


</body>
</html>

