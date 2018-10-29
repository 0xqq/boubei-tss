setTimeout( function() {
    if(window.parent && window.parent != window.self) {
        window.parent.location.href = "login.html";
    }
}, 300 );

URL_CHECK_USER = "/tss/getLoginInfo.in";
URL_LOGIN = "/tss/auth/login.do";

$(function() {     
     init();
});

var timer;
function init() {
    getParam("sysTitle", function(result) {
        if(!result) return;
        $(".sysTitle").html(result);
        document.title = result;
    });
    getParam("subTitle", function(result) {
        result && $(".subTitle").html(result);
    });
    
    getParam("regable", function(result) {
        if( result == "false" ) {
            $("#b1_reg, #b2_reg, #b3_reg, #a2").hide();
        } 
        else if( result == "REG_BDEV" ) {
            $("#b1_reg, #b3_reg").hide();
            $(" #b2_reg").width("352px").text("注 册");
        }
        else {
            getParam("regableDev", function(result) {
                if(result == "false") {
                    $("#b3_reg").hide();
                    $("#b1_reg, #b2_reg").width("45%");
                }
            });
        }
    });

    var accountEl = $1("loginName"), passwdEl  = $1("password");
    accountEl.onfocus = function() {  passwdEl.disabled = true; }
    accountEl.value = $.Cookie.getValue("iUser") || "";
    if(_user) {
        accountEl.value = _user.split("@")[0];
        passwdEl.value = _user.split("@")[1]||"";
    }
    accountEl.focus();

    $("#bt_login").click ( function() {
        doLogin(accountEl, passwdEl);
    } );

    $.Event.addEvent(document, "keydown", function(ev) {
        if(13 == ev.keyCode) { // enter
            $.Event.cancel(ev);
            $("#bt_login").focus();

            setTimeout(function() {
                doLogin(accountEl, passwdEl);
            }, 10);
        }
    });

    accountEl.onblur = function() { 
        timer = setTimeout(function(){
            var value = $(accountEl).value();
            if( !value )  return $(accountEl).focus();
            
            $.ajax({
                url: URL_CHECK_USER,
                params: {"loginName": value},
                waiting: true,
                onexcption: function() {
                    accountEl.focus();
                },
                onresult: function(){
                    accountEl.identifier = this.getNodeValue("identifier");
                    accountEl.randomKey  = this.getNodeValue("randomKey");
                    
                    passwdEl.disabled = false;
                    passwdEl.focus();
                    $(".tip").text("");
                }
            });

        }, 200);
    }
}

var doLogin = function(accountEl, passwdEl) {
    var identifier = accountEl.identifier;
    var randomKey  = accountEl.randomKey;   
    var loginName  = accountEl.value;
    var password   = passwdEl.value;
    
    if( !loginName ) {
        return $(accountEl).focus();
    } 
    else if( !password ) {
        return $(passwdEl).focus();
    }
    else if( !identifier ) {
        return $.alert("系统无法登录，服务器异常，请刷新或稍后再尝试。");
    }

    $.ajax({
        url: URL_LOGIN,
        waiting: true,
        headers : {
            "loginName": $.encode(loginName, randomKey), 
            "password":  $.encode(password, randomKey), 
            "identifier": identifier
        },
        params: { 
        	"_password": hex_md5(password)
        },
        onexception: function(errorMsg) {
            if( (errorMsg.msg||"").indexOf("账号或密码错误") >= 0 && (errorMsg.msg||"").indexOf(loginName) < 0 ) {
                location.href = 'login.html'; // 登录页面长期未刷新，验证随机数失效
            } else {
                passwdEl.focus();
            }
        },
        onsuccess: function() {
            $.Cookie.setValue("iUser", loginName);
            gotoIndex(loginName);   
        }
    });
}

var indexPage = "bi.html";    /* 登录成功后跳转到的页面 */

function gotoIndex(loginName) {
    if(window.parent && window.parent != window.self) {
        window.parent.location.href = indexPage;
    } else {
        window.location.href = indexPage;
    }
}

// 获取系统参数模块的配置信息
function getParam(key, callback) {
    $.getJSON("/tss/param/json/simple/" + key, {}, 
        function(result) {
            var val;
            if( result && result.length  && result[0] ) {
                val = result[0];
            }
            callback && callback(val);
        }, "GET", false);
}
