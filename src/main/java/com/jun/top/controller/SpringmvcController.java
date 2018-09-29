package com.jun.top.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jun.top.annotations.Autowired;
import com.jun.top.annotations.Controller;
import com.jun.top.annotations.RequestMapping;
import com.jun.top.service.SpringmvcService;

@Controller("SpringmvcController")
@RequestMapping("/springmvc")
public class SpringmvcController {

	@Autowired("SpringmvcServiceImpl")
	private SpringmvcService springmvcService;
	
	@RequestMapping("/select")
	public void find(HttpServletRequest request, HttpServletResponse response, String param) throws IOException{
		
		response.getWriter().write(springmvcService.select(param));
	}
}
