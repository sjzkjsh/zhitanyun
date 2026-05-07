package com.example.webapp.Database;
	import com.example.webapp.Entity.Result;
	import com.example.webapp.Mapper.energyMapper;
	import com.example.webapp.Service.ServiceImpl.AirConditioningAdviceService;
	import com.example.webapp.Util.UserContextUtil;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;

	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;

	import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
	@RequestMapping("/api/warning")
	public class WarningController {
		@Autowired
		private energyMapper energy;
		@Autowired
		private UserContextUtil userContextUtil;
		@Autowired
		private AirConditioningAdviceService airConditioningAdviceService;
	    /**
	     * 获取当前登录用户的建筑预警与优化策略
	     */


	    @GetMapping("/strategy")
	    public List<Double> getWarningStrategy() {
			String deviceCode = userContextUtil.getCurrentDeviceCode();
			String buildingCode = userContextUtil.getCurrentBuildingCode();
			Double envTempLatest = energy.getEnvTempLatest(buildingCode, deviceCode);
			Double humidityLatest = energy.gethumidityLatest(buildingCode, deviceCode);
			Double personnelDensityLatest = energy.getPersonnelDensityLatest(buildingCode, deviceCode);
			ArrayList<Double> doubles = new ArrayList<>();
			doubles.add(envTempLatest);
			doubles.add(personnelDensityLatest);
			doubles.add(humidityLatest);
			return doubles;
		}

	@GetMapping("/advice")
	public Result<Map<String, Object>> getAirConditioningAdvice() {
		try {
			// 获取原始建议数据
			Map<String, Object> originalAdvice = airConditioningAdviceService.getAirConditioningAdvice();

			// ✅ 转换为前端期望的格式
			Map<String, Object> formattedAdvice = formatAdviceForFrontend(originalAdvice);

			return Result.success(formattedAdvice);
		} catch (Exception e) {
			log.error("获取空调建议失败", e);
			return Result.error("获取空调建议失败");
		}
	}

	/**
	 * ✅ 添加这个格式转换方法
	 */
	private Map<String, Object> formatAdviceForFrontend(Map<String, Object> originalAdvice) {
		Map<String, Object> formatted = new HashMap<>();

		// 提取建议列表
		List<String> suggestions = (List<String>) originalAdvice.get("suggestions");

		// 默认建议
		String temperatureAdvice = "当前温度适宜，保持现有设置";
		String humidityAdvice = "湿度水平正常，无需调整";
		String densityAdvice = "人员密度适中，保持良好通风";
		String energyAdvice = "建议在非工作时间关闭不必要的设备";

		// 从suggestions列表中分类提取
		if (suggestions != null) {
			for (String suggestion : suggestions) {
				String lowerSuggestion = suggestion.toLowerCase();

				if (lowerSuggestion.contains("温度") || lowerSuggestion.contains("空调")) {
					temperatureAdvice = suggestion;
				} else if (lowerSuggestion.contains("湿度")) {
					humidityAdvice = suggestion;
				} else if (lowerSuggestion.contains("人员") || lowerSuggestion.contains("密度")) {
					densityAdvice = suggestion;
				} else if (lowerSuggestion.contains("节能") || lowerSuggestion.contains("能耗") ||
						lowerSuggestion.contains("关闭窗户") || lowerSuggestion.contains("开窗")) {
					energyAdvice = suggestion;
				}
			}
		}

		// 构建前端期望的格式
		formatted.put("temperatureAdvice", temperatureAdvice);
		formatted.put("humidityAdvice", humidityAdvice);
		formatted.put("densityAdvice", densityAdvice);
		formatted.put("energyAdvice", energyAdvice);

		// 添加环境数据
		Map<String, Object> envData = (Map<String, Object>) originalAdvice.get("environmentData");
		if (envData != null) {
			formatted.put("temperature", envData.get("temperature"));
			formatted.put("humidity", envData.get("humidity"));
			formatted.put("personnelDensity", envData.get("personnelDensity"));
		}

		return formatted;
	}
	}