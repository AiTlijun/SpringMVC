package com.jun.top.serviceImpl;

import java.util.Map;

import com.jun.top.annotations.Service;
import com.jun.top.service.SpringmvcService;

@Service("SpringmvcServiceImpl")
public class SpringmvcServiceImpl implements SpringmvcService {

	@Override
	public String select(String param) {
		
		return param + "访问成功！";
	}
	
}
