function fGoto(){}
(function(){if(!window.GC){window.GC={};GC.getUrlParamValue=function(a,b){var d=RegExp("(^|&|\\?|\\s)"+b+"\\s*=\\s*([^&]*?)(\\s|&|$)","i").exec(a);return d?d[2].replace(/[\x0f]/g,";"):""};GC.writeCookie=function(a,b,d,c,e){d=d||GC.domain;c=c||"/";if(a&&b)a=a+"="+b+";",d&&(a+="domain="+d+";"),c&&(a+="path="+c+";"),e&&(a+="expires="+e.toGMTString()+";"),document.cookie=a};GC.readCookie=function(a){var a=RegExp("(^|;)\\s*"+a+"=([^;]*)\\s*(;|$)").exec(document.cookie),b="";a!=null&&(b=decodeURIComponent(a[2]));
return b};GC.ossHit=function(){};GC.setCookie=function(a,b,d,c,e){document.cookie=a+"="+escape(b)+(d?";path="+d:"")+(e?";expires="+e.toGMTString():"")+(";domain="+(c?c:window.location.host))};GC.getCookie=function(a){a=document.cookie.match(RegExp("(^| )"+a+"=([^;]*)(;|$)"));return a!=null?unescape(a[2]):null};GC.delCookie=function(a,b,d){var d=d||window.location.host,c=new Date;c.setTime(c.getTime()-1E4);if(GC.getCookie(a))document.cookie=a+"="+(b?";path="+b:"")+(d?";domain="+d:"")+";expires="+c.toGMTString()};
GC.gRnd=function(){var a=new Date;return Math.random()+","+a.getMilliseconds()};GC.rfUrl=function(a){var b=GC.gRnd();a.indexOf("&rnd=")>0?a=a.substr(0,a.indexOf("&rnd=")):a.indexOf("?rnd=")>0&&(a=a.substr(0,a.indexOf("?rnd=")));a+=a.indexOf("?")>0?"&rnd="+b:"?rnd="+b;return a};GC.appendUrl=function(a,b,d){b&&d&&(d=encodeURI(d),a+=a.indexOf("?")>0?"&"+b+"="+d:"?"+b+"="+d);return a};GC.appendParam=function(a,b,d){var c=[],e="?";a.indexOf("?")>0&&(e="&");if(b)for(var f in b)b[f]&&c.push(f+"="+encodeURI(b[f]));
c.length>0&&(a+=e+c.join("&"));d&&(a=GC.rfUrl(a));return a};GC.getScript=function(a,b,d,c){var e=document.getElementById(a);c||(c={});var f=c.isdefer||!1,h=c.charset||"gb2312";c.isrnd&&(b=GC.rfUrl(b));c=document.getElementsByTagName("head")[0];e!=null&&(c.removeChild(e),e=null);e=document.createElement("script");if(typeof d=="function")document.all?e.onreadystatechange=function(){(e.readyState=="loaded"||e.readyState=="complete")&&d()}:e.onload=function(){d()};h=h||"gb2312";e.setAttribute("id",a);
e.setAttribute("charset",h);e.setAttribute("src",b);f&&e.setAttribute("defer",!1);e.setAttribute("type","text/javascript");c.appendChild(e)};GC.loadScript=function(a,b,d){if(b){b.substring(0,4)!="http"&&(b=GC.frmPath+"/js/"+b);var c=document.getElementById(a),e=document.getElementsByTagName("head")[0];c!=null&&e.removeChild(c);c=document.createElement("script");d=d||"gb2312";c.setAttribute("id",a);c.setAttribute("charset",d);c.setAttribute("src",b);c.setAttribute("type","text/javascript");e.appendChild(c)}};
GC.loadCss=function(a,b,d){if(b){b.substring(0,4)!="http"&&(b=GC.top.gMain.frmPath+"/css/"+b);var c=document.getElementById(a),e=document.getElementsByTagName("head")[0];c!=null&&e.removeChild(c);c=document.createElement("link");c.setAttribute("id",a);c.setAttribute("rel","stylesheet");c.setAttribute("type","text/css");c.setAttribute("href",b);d&&c.setAttribute("charset",d);e.appendChild(c)}};GC.logoff=function(){window.location.replace(GC.returnUrl);return!1};GC.fireDocReady=function(a,b){var d=
b||document,c=b||window,e=c.onload,f=function(){typeof e=="function"&&e.toString()!=f.toString()&&e();typeof a=="function"&&a()};if(document.all){var h=function(){d.readyState=="complete"?f():setTimeout(h,10)};setTimeout(h,10)}c.onload=f};GC.fireEvent=function(a,b){if(a)if(document.all)a["on"+b]();else{var d=document.createEvent("Events");d.initEvent(b,!1,!0);a.dispatchEvent(d)}};GC.textChange=function(a){if(a)if(document.all)a.fireEvent("onchange");else{var b=document.createEvent("Events");b.initEvent("change",
!1,!0);typeof a.dispatchEvent=="function"&&a.dispatchEvent(b)}};GC.checkModule=function(){return!1};GC.check=function(a){return GC.power[a]||!1};GC.initExtend=function(){try{GC.mainFrm&&GC.mainFrm.Object.extend&&(GC.mainFrm.Object.extend(Number.prototype,GC.mainFrm.Number.prototype),GC.mainFrm.Object.extend(String.prototype,GC.mainFrm.String.prototype),GC.mainFrm.Object.extend(Array.prototype,GC.mainFrm.Array.prototype),GC.mainFrm.Object.extend(Date.prototype,GC.mainFrm.Date.prototype),GC.mainFrm.Object.extend(Function.prototype,
GC.mainFrm.Function.prototype))}catch(a){}};GC.initText=function(a){function b(a){if(a)a.className="text",GC.top.EV.observe(a,"focus",function(){GC.top.El.addClass(a,"focus")},!1),GC.top.EV.observe(a,"blur",function(){GC.top.El.removeClass(a,"focus")},!1)}if(document.all){var d=GC.top.El.getNodeType(a,!0);if(d=="text"||d=="textarea")b(a);else for(var a=a.getElementsByTagName("input"),c=0;c<a.length;c++){var e=a[c],d=GC.top.El.getNodeType(e,!0);(d=="text"||d=="textarea")&&b(e)}}};try{var i=GC.getCookie("maindomain");
if(i&&i!=location.host)GC.olddm=document.domain,document.domain=i;GC.domain=i}catch(j){}GC.top=function(){for(var a=window,b=GC.getUrlParamValue(location.href,"sid"),d=a.gMain,c=0;!(d&&d.sid==b)&&c<3;)a=a.parent,d=a.gMain,c++;return a}();GC.mainFrm=GC.top;if(window!=GC.top&&!/(login.do|m.do|\/history.html?|\/blank.html?)/i.exec(window.location))GC.sid=GC.top.gMain.sid,GC.power=GC.top.gPower,GC.isInit=!0;else if(window.gMain)GC.sid=gMain.sid,GC.power=GC.top.gPower,GC.isInit=!1}})();GC.isInit&&GC.initExtend();
function g(){};
