package com.xin.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.xin.service.UserServiceImpl;
import com.xin.vo.User;

@WebServlet(
		name="/UserControl",
		urlPatterns={"/UserControl"},
		initParams={
				@WebInitParam(name="show", value="show.jsp")
		}
)
public class UserControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SHOW = "show";
    private UserServiceImpl service = new UserServiceImpl();   
    private Map<String,String> map = new HashMap<String,String>(); //存跳转页信息
    
    public UserControl() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    	map.put(SHOW, config.getInitParameter(SHOW));
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			login(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void login(HttpServletRequest request, HttpServletResponse response) {
		//传递账户信息
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		
		//携带特定信息跳转到指定页面，在table.jsp的操作信息中打印提示信息
 		RequestDispatcher dispatcher = request.getRequestDispatcher(map.get(SHOW));
 		if(service.login(user)) { //登录成功
        	//添加cookie
 			if(request.getParameter("sun") != null) { //若用户勾选了要记住账户信息
 				Cookie c1 = new Cookie("http://www.bbs.com/username", username);
 				Cookie c2 = new Cookie("http://www.bbs.com/password", password);
 				c1.setMaxAge(7 * 24 * 3600); //时限为一周
 				c2.setMaxAge(7 * 24 * 3600); 
 				response.addCookie(c1);
 				response.addCookie(c2);
 			}
 			
 		
        	request.setAttribute("msg", "呵呵，perfect！欢迎" + username + "进入论坛！");
        } else {  //登录失败
        	request.setAttribute("msg", "啊偶，Oh my God！ 请重新登录！");
        }
        try {
			dispatcher.forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

	
}
