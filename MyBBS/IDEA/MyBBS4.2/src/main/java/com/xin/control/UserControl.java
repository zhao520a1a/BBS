package com.xin.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.xin.service.UserServiceImpl;
import com.xin.vo.User;

@WebServlet(
		name="/UserControl",
		urlPatterns={"/UserControl"},
		initParams={  
				@WebInitParam(name="show", value="/ArticleControl?action=query&curPage=1")
		}
)
public class UserControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SHOW = "show";
    private UserServiceImpl service = new UserServiceImpl();   
    private Map<String,String> map = new HashMap<String,String>(); //存跳转页信息
	private RequestDispatcher dispatcher = null;
    
    public UserControl() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    	map.put(SHOW, config.getInitParameter(SHOW));
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//检查文件上传的类型请求是否是 enctype="multipart/form-data",即：采用二进制流的方式
		if(ServletFileUpload.isMultipartContent(request)) {
			register(request, response);
		} 
		
		String action = request.getParameter("action");
		if(action != null){ //??
		switch(action) {
		case "login" : //请求登录
			login(request, response);
			break;
		case "pic": //显示用户头像
			String id = request.getParameter("id");
			//通过用户id从数据库得到用户头像的路径
			byte[] buffer = service.getPic(Integer.parseInt(id));
			response.setContentType("image/jpeg"); //告诉浏览器该数据是图片格式的
			//向客户端传输用户头像
			ServletOutputStream out = response.getOutputStream();
			out.write(buffer);
			out.flush();
			out.close();
		    break;
		case "page": //请求每页显示的数据条数
			int row = Integer.parseInt(request.getParameter("row"));
			int userid = Integer.parseInt(request.getParameter("userid"));
			if(service.editPageNum(row, userid)){
				//携带特定信息跳转到指定页面，因为跳转地址中没有userid，这里组合加上userid
				dispatcher = request.getRequestDispatcher(map.get(SHOW).toString() + "&userid=" + userid);
				dispatcher.forward(request, response);
			}
		default:
			break;
		}
		}
		
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
		
 		User returnUser = service.login(user);
 		if(returnUser != null) { //登录成功
 			//携带特定信息跳转到指定页面，因为跳转地址中没有userid，这里组合加上userid
 	 		dispatcher = request.getRequestDispatcher(map.get(SHOW).toString() + "&userid=" + returnUser.getId());
 			
        	//添加cookie
 			if(request.getParameter("sun") != null) { //若用户勾选了要记住账户信息
 				Cookie c1 = new Cookie("http://www.bbs.com/username", username);
 				Cookie c2 = new Cookie("http://www.bbs.com/password", password);
 				c1.setMaxAge(7 * 24 * 3600); //时限为一周
 				c2.setMaxAge(7 * 24 * 3600); 
 				response.addCookie(c1);
 				response.addCookie(c2);
 			}
 			
 			//将user中封装信息放在Session域中，供以后前端调用，例如：为获得数据库中的用户头像向浏览器提供用户id信息；
 			request.getSession().setAttribute("user",  returnUser);
        	request.setAttribute("msg", "呵呵，perfect！欢迎" + username + "进入论坛！");
        } else {  //登录失败
        	request.setAttribute("msg", "啊偶，Oh my God！ 请重新登录！");
        }
      
		try {
			dispatcher.forward(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void register(HttpServletRequest request, HttpServletResponse response) {
		User user = service.uploadPic(request);
		
		if(service.register(user)) {
			request.setAttribute("msg", "注册成功！");
			dispatcher=request.getRequestDispatcher(map.get(SHOW));
			try {
				dispatcher.forward(request, response);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
	}
	
}
