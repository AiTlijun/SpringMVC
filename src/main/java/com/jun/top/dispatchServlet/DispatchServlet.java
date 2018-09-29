package com.jun.top.dispatchServlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.omg.Dynamic.Parameter;

import com.jun.top.annotations.Autowired;
import com.jun.top.annotations.Controller;
import com.jun.top.annotations.RequestMapping;
import com.jun.top.annotations.Service;
import com.jun.top.controller.SpringmvcController;

public class DispatchServlet extends HttpServlet {

	//把所有包下面的class集合
	List<String> classList = new ArrayList<String>();
	//类实例化集合
	private Map<String, Object> instanceMap = new HashMap<String, Object>();
	//类中方法对应映射集合
	private Map<String, Object> handerMap = new HashMap<String, Object>();
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("调用get");
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("调用post");
		String url = req.getRequestURI();
		System.out.println("调用post url:"+ url);
        String context = req.getContextPath();
        String path = url.replace(context, "");
        System.out.println("调用post url:"+ url);
        System.out.println("调用post context:"+ context);
        System.out.println("调用post path:"+ path);
        Method handlerMethod = (Method) handerMap.get(path);
        SpringmvcController controller = (SpringmvcController) instanceMap.get("SpringmvcController");
        String param = "打飞机";
        if(!this.instanceMap.containsKey("SpringmvcController")){
            resp.getWriter().write("404 NOT FOUND!");
            return;
          }
     // 获取方法的参数列表
        Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
        // 调用方法需要传递的形参
        Object paramValues[] = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {

            if (ServletRequest.class.isAssignableFrom(parameterTypes[i])) {
                paramValues[i] = req;
            } else if (ServletResponse.class.isAssignableFrom(parameterTypes[i])) {
                paramValues[i] = resp;
            } else {// 其它参数，目前只支持String，Integer，Float，Double
                // 参数绑定的名称，默认为方法形参名
                String bindingValue = parameterTypes[i].getName();
                // 从请求中获取参数的值
                String paramValue = req.getParameter(bindingValue);
                paramValues[i] = paramValue;
                if (paramValue != null) {
                    if (Integer.class.isAssignableFrom(parameterTypes[i])) {
                        paramValues[i] = Integer.parseInt(paramValue);
                    } else if (Float.class.isAssignableFrom(parameterTypes[i])) {
                        paramValues[i] = Float.parseFloat(paramValue);
                    } else if (Double.class.isAssignableFrom(parameterTypes[i])) {
                        paramValues[i] = Double.parseDouble(paramValue);
                    } else if (String.class.isAssignableFrom(parameterTypes[i])) {
                        paramValues[i] = Double.parseDouble(paramValue);
                    }
                }        	System.out.println("paramValue===="+paramValue);

            }
        }
        try {
        	handlerMethod.invoke(controller, new Object[] { req, resp, "test" });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
	}

	@Override
	public void init() throws ServletException {
		System.out.println("-----------init---------");
		// 扫描包下类
		scanPackage("com.jun.top");
		//反射机制实例化对象
		try {
			filterAndInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//controller类中方法映射
		handerMap();
		//类中属性依赖注入
		try {
			ioc();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String string : classList) {
			System.out.println("----------"+ string);
		}
		System.out.println("------------------");
		Set<Entry<String, Object>> entrySet = instanceMap.entrySet();
		for(Entry<String, Object> entrySetNew : entrySet){
		System.out.println("------"+ entrySetNew.getKey()+"  -----"+entrySetNew.getValue());
		}
		System.out.println("------------------");
		Set<Entry<String, Object>> entrySet2 = handerMap.entrySet();
		for(Entry<String, Object> entrySetNew : entrySet2){
		System.out.println("------"+ entrySetNew.getKey()+"  -----"+entrySetNew.getValue());
		}
	}
	

	private void ioc() throws IllegalArgumentException, IllegalAccessException {
		if (instanceMap.isEmpty())
            return;
		Set<Entry<String, Object>> entrySet = instanceMap.entrySet();
		for(Entry<String, Object> entrySetNew : entrySet){
			Field[] declaredFields = entrySetNew.getValue().getClass().getDeclaredFields();
			for (Field field : declaredFields) {
				field.setAccessible(true);
				if(field.isAnnotationPresent(Autowired.class)){
					field.setAccessible(true);
					Autowired autowired = field.getAnnotation(Autowired.class);
					field.set(entrySetNew.getValue(), instanceMap.get(autowired.value()));
				}
			}
		}
	}

	private void handerMap() {
		if(instanceMap.isEmpty()){
			return;
		}
		Set<Entry<String, Object>> entrySet = instanceMap.entrySet();
		for(Entry<String, Object> entrySetNew : entrySet){
			if(entrySetNew.getValue().getClass().isAnnotationPresent(Controller.class)){
				Controller controller = entrySetNew.getValue().getClass().getAnnotation(Controller.class);
				String controllerMappingStr = "";
				if(entrySetNew.getValue().getClass().isAnnotationPresent(RequestMapping.class)) {
					RequestMapping controllerMapping = entrySetNew.getValue().getClass().getAnnotation(RequestMapping.class);
					controllerMappingStr = controllerMapping.value();
				}
				Method[] methods = entrySetNew.getValue().getClass().getMethods();
				for (Method method : methods) {
					if(method.isAnnotationPresent(RequestMapping.class)){
						RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
						handerMap.put(controllerMappingStr + requestMapping.value(), method);
					}
					else{
						continue;
					}
				}
			}else{
				continue;
			}
		}
	}

	private void filterAndInstance() throws Exception {
		
		if(classList.isEmpty()){
			return;
		}
		for(String className: classList){
			Class<?> classNew = Class.forName(className.replace(".class", "").trim());
			if(classNew.isAnnotationPresent(Controller.class)){
				Controller controller = classNew.getAnnotation(Controller.class);
				Object newInstance = classNew.newInstance();
				instanceMap.put(controller.value(), newInstance);
			}
			if(classNew.isAnnotationPresent(Service.class)){
				Service service = classNew.getAnnotation(Service.class);
				Object newInstance = classNew.newInstance();
				instanceMap.put(service.value(), newInstance);
			}
		}
		
	}

	private void scanPackage(String Package) {
		URL url = this.getClass().getClassLoader()
				.getResource("/" + replaceTo(Package));// 将所有的.转义获取对应的路径
		String pathFile = url.getFile();
		File file = new File(pathFile);
		String fileList[] = file.list();
		for (String path : fileList) {
			File eachFile = new File(pathFile + path);
			if (eachFile.isDirectory()) {
				scanPackage(Package + "." + eachFile.getName());
			} else {
				classList.add(Package + "." + eachFile.getName());
			}
		}
	}
	private String replaceTo(String path) {
        return path.replaceAll("\\.", "/");
    }

}
