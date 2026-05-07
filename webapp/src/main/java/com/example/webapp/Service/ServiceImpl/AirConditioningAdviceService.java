package com.example.webapp.Service.ServiceImpl;
	import com.example.webapp.Entity.Weather.DailyWeather;
	import com.example.webapp.Entity.Weather.WeatherVO;
	import com.example.webapp.Entity.customer;
	import com.example.webapp.Mapper.energyMapper;
	import com.example.webapp.Service.LoginService;
	import com.example.webapp.Service.WeatherService.WeatherService;
	import com.example.webapp.Util.LoginCustomerHolder;
	import lombok.extern.slf4j.Slf4j;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Service;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
	/**
	 * 空调使用建议服务
	 * 综合天气数据和室内环境参数提供智能化的空调使用建议
	 */
	@Service
	@Slf4j
	public class AirConditioningAdviceService {
	    @Autowired
	    private WeatherService weatherService;
	    @Autowired
	    private energyMapper energyReadingsMapper;
	    @Autowired
	    private LoginService loginService;
	    /**
	     * 获取空调使用建议
	     * @return 包含天气数据、环境参数和建议的完整信息
	     */
	    public Map<String, Object> getAirConditioningAdvice() {
	        // 获取当前登录用户信息
	        Long userId = LoginCustomerHolder.getLoginCustomer().getId();
	        customer user = loginService.getById(userId);
	        if (user == null) {
	            throw new RuntimeException("用户信息获取失败");
	        }
	        // 获取环境数据
	        Double envTemp = energyReadingsMapper.getEnvTempLatest(user.getBuildingCode(), user.getDeviceCode());
	        Double humidity = energyReadingsMapper.gethumidityLatest(user.getBuildingCode(), user.getDeviceCode());
	        Double personnelDensity = energyReadingsMapper.getPersonnelDensityLatest(user.getBuildingCode(), user.getDeviceCode());
	        // 获取天气数据（以郑州为例）
	        WeatherVO weatherData = weatherService.getWeather("郑州");
	        // 生成建议
	        Map<String, Object> advice = new HashMap<>();
	        advice.put("userData", Map.of(
	            "name", user.getName(),
	            "buildingCode", user.getBuildingCode(),
	            "deviceCode", user.getDeviceCode()
	        ));
	        advice.put("environmentData", Map.of(
	            "temperature", envTemp,
	            "humidity", humidity,
	            "personnelDensity", personnelDensity
	        ));
	        advice.put("weatherData", weatherData);
	        // 生成具体建议
	        List<String> suggestions = generateSuggestions(
	            envTemp, humidity, personnelDensity, weatherData);
	        // 分析优先级
	        String priority = analyzePriority(suggestions);
	        advice.put("suggestions", suggestions);
	        advice.put("priority", priority);
	        advice.put("summary", generateSummary(envTemp, humidity, personnelDensity, weatherData));
	        return advice;
	    }
	    /**
	     * 生成具体建议
	     */
	    private List<String> generateSuggestions(Double envTemp, Double humidity, 
	                                           Double personnelDensity, WeatherVO weatherData) {
	        List<String> suggestions = new ArrayList<>();
	        // 1. 温度相关建议
	        if (envTemp == null) {
	            suggestions.add("无法获取室内温度，请检查设备连接");
	        } else if (envTemp < 20) {
	            suggestions.add("室内温度较低(" + envTemp + "°C)，建议提高空调温度至22-24℃");
	        } else if (envTemp > 28) {
	            suggestions.add("室内温度较高(" + envTemp + "°C)，建议降低空调温度至24-26℃");
	        } else {
	            suggestions.add("室内温度适宜(" + envTemp + "°C)，建议保持空调温度在24-26℃");
	        }
	        // 2. 湿度相关建议
	        if (humidity == null) {
	            suggestions.add("无法获取室内湿度数据");
	        } else if (humidity < 30) {
	            suggestions.add("室内湿度过低(" + humidity + "%)，建议开启加湿功能或放置加湿器");
	        } else if (humidity > 70) {
	            suggestions.add("室内湿度过高(" + humidity + "%)，建议开启除湿模式");
	        } else {
	            suggestions.add("室内湿度适宜(" + humidity + "%)，保持在40%-60%为最佳");
	        }
	        // 3. 人员密度相关建议
	        if (personnelDensity == null) {
	            suggestions.add("无法获取人员密度数据");
	        } else if (personnelDensity > 80) {
	            suggestions.add("人员密度高(" + personnelDensity + "%)，建议增强通风或增加空调风量");
	        } else if (personnelDensity < 30) {
	            suggestions.add("人员密度低(" + personnelDensity + "%)，可适当降低空调运行负荷以节能");
	        } else {
	            suggestions.add("人员密度适中(" + personnelDensity + "%)，建议保持标准风量");
	        }
	        // 4. 外部天气与内部环境协调建议
	        DailyWeather todayWeather = weatherData.getForecast().get(0);
	        if (todayWeather != null) {
	            int outdoorTemp = (todayWeather.getTempHigh() + todayWeather.getTempLow()) / 2;
	            String weatherCondition = todayWeather.getWeather();
	            if (outdoorTemp < envTemp - 5) {
	                suggestions.add("室外温度较低(" + outdoorTemp + "℃)，可适当开窗通风减少空调使用");
	            } else if (outdoorTemp > envTemp + 5) {
	                suggestions.add("室外温度较高(" + outdoorTemp + "℃)，建议关闭窗户避免热空气进入");
	            }
	            if (weatherCondition.contains("雨") || weatherCondition.contains("雪")) {
	                suggestions.add("雨天/雪天建议保持空调运行，避免室内湿度过高");
	            }
	            if (weatherCondition.contains("晴") && outdoorTemp > 30) {
	                suggestions.add("高温晴天(" + outdoorTemp + "℃)，建议提前开启空调降温");
	            }
	        }
	        return suggestions;
	    }
	    /**
	     * 分析建议优先级
	     */
	    private String analyzePriority(List<String> suggestions) {
	        // 根据问题数量确定优先级
	        int criticalCount = (int) suggestions.stream()
	            .filter(s -> s.contains("温度较高") || s.contains("温度较低") || 
	                        s.contains("湿度过高") || s.contains("湿度过低"))
	            .count();
	        if (criticalCount >= 2) {
	            return "高";
	        } else if (criticalCount == 1 || suggestions.size() >= 4) {
	            return "中";
	        } else {
	            return "低";
	        }
	    }
	    /**
	     * 生成摘要信息
	     */
	    private String generateSummary(Double envTemp, Double humidity, Double personnelDensity, WeatherVO weatherData) {
	        StringBuilder summary = new StringBuilder();
	        if (envTemp != null) {
	            summary.append("当前温度: ").append(envTemp).append("℃");
	        }
	        if (humidity != null) {
	            summary.append(", 湿度: ").append(humidity).append("%");
	        }
	        if (personnelDensity != null) {
	            summary.append(", 人员密度: ").append(personnelDensity).append("%");
	        }
	        if (weatherData != null) {
	            DailyWeather todayWeather = weatherData.getForecast().get(0);
	            if (todayWeather != null) {
	                summary.append(", 今日天气: ").append(todayWeather.getWeather());
	            }
	        }
	        summary.append("。");
	        // 添加关键信息
	        if (envTemp != null && envTemp > 28) {
	            summary.append(" 室内温度偏高，建议调整。");
	        }
	        if (humidity != null && (humidity < 30 || humidity > 70)) {
	            summary.append(" 湿度异常，需要调节。");
	        }
	        if (personnelDensity != null && personnelDensity > 80) {
	            summary.append(" 人员密度较高，请加强通风。");
	        }
	        return summary.toString();
	    }
	}