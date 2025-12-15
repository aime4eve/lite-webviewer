"""
霉菌生长概率预测算法
Mold Growth Probability Prediction Algorithm

基于环境数据（温度、湿度、天气等）预测霉菌生长概率
支持实时预测和短期预报（1-72小时）
"""

import numpy as np
import pandas as pd
from datetime import datetime, timedelta
from typing import Dict, List, Tuple, Optional
import math
import warnings
warnings.filterwarnings('ignore')

class MoldPredictionModel:
    """霉菌生长概率预测模型"""
    
    def __init__(self):
        """初始化模型参数"""
        # 霉菌生长最优条件（基于科学研究）
        self.optimal_temp_range = (20, 30)  # 最优温度范围（°C）
        self.optimal_humidity_range = (75, 95)  # 最优湿度范围（%RH）
        self.min_temp = 5  # 最低生长温度（°C）
        self.max_temp = 40  # 最高生长温度（°C）
        self.min_humidity = 65  # 最低生长湿度（%RH）
        
        # 各因子的权重（总和为1）
        self.weights = {
            'temperature': 0.25,      # 温度权重
            'humidity': 0.35,         # 湿度权重（最高）
            'time_factor': 0.15,      # 时间因子
            'surface_factor': 0.10,   # 表面因子
            'weather_factor': 0.10,   # 天气因子
            'ventilation_factor': 0.05 # 通风因子
        }
        
        # 季节因子（影响霉菌生长速率）
        self.season_factors = {
            'spring': 1.0,    # 春季：适中
            'summer': 1.3,    # 夏季：高发
            'autumn': 0.9,    # 秋季：中等
            'winter': 0.6     # 冬季：低发
        }
        
        # 天气因子
        self.weather_factors = {
            'sunny': 0.7,     # 晴天：抑制霉菌
            'cloudy': 1.0,    # 多云：正常
            'rainy': 1.4,     # 雨天：促进霉菌
            'snowy': 0.8,     # 雪天：轻微抑制
            'foggy': 1.5,     # 雾天：高湿促进
            'stormy': 1.6     # 暴风雨：极端促进
        }
        
        # 表面材料因子
        self.surface_factors = {
            'tile': 0.7,      # 瓷砖：不易发霉
            'paint': 1.0,     # 涂料：中等
            'wallpaper': 1.3, # 壁纸：易发霉
            'concrete': 1.1,  # 混凝土：较易发霉
            'wood': 1.4,      # 木材：易发霉
            'carpet': 1.6     # 地毯：极易发霉
        }

    def calculate_temperature_score(self, temp: float) -> float:
        """
        计算温度因子得分（0-1）
        
        Args:
            temp: 温度（°C）
            
        Returns:
            温度因子得分（0-1），越接近最优温度得分越高
        """
        if temp < self.min_temp or temp > self.max_temp:
            return 0.0
        
        # 在最优温度范围内，得分为1.0
        if self.optimal_temp_range[0] <= temp <= self.optimal_temp_range[1]:
            return 1.0
        
        # 在边界区域，得分线性递减
        if temp < self.optimal_temp_range[0]:
            # 从min_temp到optimal_temp_range[0]线性递增
            score = (temp - self.min_temp) / (self.optimal_temp_range[0] - self.min_temp)
        else:  # temp > self.optimal_temp_range[1]
            # 从optimal_temp_range[1]到max_temp线性递减
            score = (self.max_temp - temp) / (self.max_temp - self.optimal_temp_range[1])
        
        return min(max(score, 0.0), 1.0)

    def calculate_humidity_score(self, humidity: float) -> float:
        """
        计算湿度因子得分（0-1）
        
        Args:
            humidity: 相对湿度（%RH）
            
        Returns:
            湿度因子得分（0-1）
        """
        if humidity < self.min_humidity:
            return 0.0
        
        # 在最优湿度范围内，得分为1.0
        if self.optimal_humidity_range[0] <= humidity <= self.optimal_humidity_range[1]:
            return 1.0
        
        # 高于最优湿度范围，得分继续增加（但增速减缓）
        if humidity > self.optimal_humidity_range[1]:
            base_score = 1.0
            extra_score = (humidity - self.optimal_humidity_range[1]) / 100.0
            return min(base_score + extra_score, 1.5)  # 最高1.5分，但会被权重限制
        
        # 低于最优湿度范围
        score = (humidity - self.min_humidity) / (self.optimal_humidity_range[0] - self.min_humidity)
        return min(max(score, 0.0), 1.0)

    def calculate_time_factor(self, hour: int, season: str = 'spring') -> float:
        """
        计算时间因子得分
        
        Args:
            hour: 小时（0-23）
            season: 季节（spring/summer/autumn/winter）
            
        Returns:
            时间因子得分（0-1）
        """
        # 一天中湿度较高的时段（夜间和清晨）
        high_humidity_hours = [0, 1, 2, 3, 4, 5, 6, 22, 23]
        
        base_factor = 0.6
        if hour in high_humidity_hours:
            base_factor = 0.9
        
        # 应用季节因子
        season_factor = self.season_factors.get(season, 1.0)
        
        return min(base_factor * season_factor, 1.0)

    def calculate_surface_factor(self, wall_material: str = 'paint') -> float:
        """
        计算表面材料因子
        
        Args:
            wall_material: 墙面材料
            
        Returns:
            表面因子得分（0-1）
        """
        return self.surface_factors.get(wall_material, 1.0)

    def calculate_weather_factor(self, weather: str = 'cloudy') -> float:
        """
        计算天气因子
        
        Args:
            weather: 天气状况
            
        Returns:
            天气因子得分（0-1）
        """
        return self.weather_factors.get(weather, 1.0)

    def calculate_ventilation_factor(self, ventilation_rate: float = 0.5) -> float:
        """
        计算通风因子（抑制霉菌生长）
        
        Args:
            ventilation_rate: 通风换气率（次/小时）
            
        Returns:
            通风因子（0-1），通风越好，因子越小
        """
        # 通风换气率越高，对霉菌的抑制效果越好
        if ventilation_rate >= 2.0:  # 良好通风
            return 0.3
        elif ventilation_rate >= 1.0:  # 一般通风
            return 0.6
        elif ventilation_rate >= 0.5:  # 较差通风
            return 0.8
        else:  # 几乎无通风
            return 1.0

    def calculate_dew_point(self, temp: float, humidity: float) -> float:
        """
        计算露点温度
        
        Args:
            temp: 温度（°C）
            humidity: 相对湿度（%RH）
            
        Returns:
            露点温度（°C）
        """
        # Magnus公式计算露点温度
        a = 17.27
        b = 237.7
        
        alpha = ((a * temp) / (b + temp)) + math.log(humidity / 100.0)
        dew_point = (b * alpha) / (a - alpha)
        
        return dew_point

    def calculate_condensation_risk(self, temp: float, humidity: float, wall_temp: float) -> float:
        """
        计算结露风险
        
        Args:
            temp: 空气温度（°C）
            humidity: 相对湿度（%RH）
            wall_temp: 墙体表面温度（°C）
            
        Returns:
            结露风险评分（0-1）
        """
        dew_point = self.calculate_dew_point(temp, humidity)
        temp_diff = wall_temp - dew_point
        
        if temp_diff > 5:  # 墙体温度远高于露点
            return 0.0
        elif temp_diff > 2:  # 轻微结露风险
            return 0.3
        elif temp_diff > 0:  # 中等结露风险
            return 0.7
        else:  # 高风险结露
            return 1.0

    def predict_mold_growth_probability(self, 
                                      room_temp: float,
                                      room_humidity: float,
                                      wall_temp: Optional[float] = None,
                                      outdoor_temp: Optional[float] = None,
                                      outdoor_humidity: Optional[float] = None,
                                      weather: str = 'cloudy',
                                      wall_material: str = 'paint',
                                      ventilation_rate: float = 0.5,
                                      hour: int = 12,
                                      season: str = 'spring',
                                      days_since_cleaning: int = 7) -> Dict:
        """
        预测霉菌生长概率
        
        Args:
            room_temp: 室内温度（°C）
            room_humidity: 室内相对湿度（%RH）
            wall_temp: 墙体表面温度（°C），默认等于室温
            outdoor_temp: 室外温度（°C）
            outdoor_humidity: 室外相对湿度（%RH）
            weather: 天气状况
            wall_material: 墙面材料
            ventilation_rate: 通风换气率（次/小时）
            hour: 当前小时（0-23）
            season: 季节
            days_since_cleaning: 距离上次清洁天数
            
        Returns:
            包含霉菌生长概率和各因子得分的字典
        """
        # 参数默认值处理
        if wall_temp is None:
            wall_temp = room_temp - 1.0  # 墙体温度通常比室温低1-2度
        
        # 计算各因子得分
        temp_score = self.calculate_temperature_score(room_temp)
        humidity_score = self.calculate_humidity_score(room_humidity)
        time_factor = self.calculate_time_factor(hour, season)
        surface_factor = self.calculate_surface_factor(wall_material)
        weather_factor = self.calculate_weather_factor(weather)
        ventilation_factor = self.calculate_ventilation_factor(ventilation_rate)
        
        # 计算结露风险
        condensation_risk = self.calculate_condensation_risk(room_temp, room_humidity, wall_temp)
        
        # 考虑清洁因素（时间越长，霉菌风险越高）
        cleaning_factor = min(1.0 + (days_since_cleaning - 7) * 0.05, 1.5)
        
        # 综合计算霉菌生长概率
        base_probability = (
            temp_score * self.weights['temperature'] +
            humidity_score * self.weights['humidity'] +
            time_factor * self.weights['time_factor'] +
            surface_factor * self.weights['surface_factor'] +
            weather_factor * self.weights['weather_factor'] +
            (2.0 - ventilation_factor) * self.weights['ventilation_factor']  # 通风因子反向
        )
        
        # 应用结露风险和清洁因子
        final_probability = base_probability * (1.0 + condensation_risk * 0.3) * cleaning_factor / 1.5
        
        # 确保概率在0-1范围内
        final_probability = min(max(final_probability, 0.0), 1.0)
        
        # 确定风险等级
        risk_level = self.get_risk_level(final_probability)
        
        return {
            'mold_risk_score': round(final_probability, 3),
            'risk_level': risk_level,
            'confidence': 0.85,  # 模型置信度
            'timestamp': datetime.now().isoformat(),
            'factors': {
                'temperature_score': round(temp_score, 3),
                'humidity_score': round(humidity_score, 3),
                'time_factor': round(time_factor, 3),
                'surface_factor': round(surface_factor, 3),
                'weather_factor': round(weather_factor, 3),
                'ventilation_factor': round(ventilation_factor, 3),
                'condensation_risk': round(condensation_risk, 3),
                'cleaning_factor': round(cleaning_factor, 3)
            },
            'environmental_data': {
                'room_temp': room_temp,
                'room_humidity': room_humidity,
                'wall_temp': wall_temp,
                'dew_point': round(self.calculate_dew_point(room_temp, room_humidity), 2)
            }
        }

    def get_risk_level(self, probability: float) -> str:
        """
        根据概率确定风险等级
        
        Args:
            probability: 霉菌生长概率（0-1）
            
        Returns:
            风险等级字符串
        """
        if probability < 0.2:
            return "极低风险"
        elif probability < 0.4:
            return "低风险"
        elif probability < 0.6:
            return "中等风险"
        elif probability < 0.8:
            return "高风险"
        else:
            return "极高风险"

    def forecast_mold_risk(self, 
                          current_data: Dict,
                          forecast_hours: int = 24) -> List[Dict]:
        """
        预测未来一段时间的霉菌风险
        
        Args:
            current_data: 当前环境数据
            forecast_hours: 预测时长（小时）
            
        Returns:
            预测结果列表
        """
        predictions = []
        base_temp = current_data['room_temp']
        base_humidity = current_data['room_humidity']
        
        for hour in range(1, forecast_hours + 1):
            # 模拟未来环境变化（简化模型）
            # 实际应用中，这里应该使用天气预报数据
            
            # 温度变化（昼夜变化）
            hour_of_day = (datetime.now().hour + hour) % 24
            if 6 <= hour_of_day <= 18:  # 白天
                temp_variation = np.random.normal(0, 1.0)  # 白天温度略高
            else:  # 夜间
                temp_variation = np.random.normal(-2, 1.0)  # 夜间温度降低
            
            # 湿度变化（与温度反向）
            humidity_variation = np.random.normal(0, 2.0)
            
            forecast_temp = base_temp + temp_variation
            forecast_humidity = min(max(base_humidity + humidity_variation, 30), 95)
            
            # 预测霉菌风险
            prediction = self.predict_mold_growth_probability(
                room_temp=forecast_temp,
                room_humidity=forecast_humidity,
                hour=hour_of_day,
                **{k: v for k, v in current_data.items() if k not in ['room_temp', 'room_humidity']}
            )
            
            predictions.append({
                'timestamp': (datetime.now() + timedelta(hours=hour)).isoformat(),
                'forecast_hour': hour,
                'mold_risk_score': prediction['mold_risk_score'],
                'risk_level': prediction['risk_level'],
                'predicted_temp': round(forecast_temp, 1),
                'predicted_humidity': round(forecast_humidity, 1)
            })
        
        return predictions

    def generate_alert(self, prediction: Dict) -> Optional[Dict]:
        """
        根据预测结果生成预警信息
        
        Args:
            prediction: 预测结果
            
        Returns:
            预警信息（如果风险较高）
        """
        risk_score = prediction['mold_risk_score']
        
        if risk_score < 0.4:
            return None  # 低风险不预警
        
        alert_levels = {
            '中等风险': 'warning',
            '高风险': 'critical',
            '极高风险': 'critical'
        }
        
        risk_level = prediction['risk_level']
        if risk_level not in alert_levels:
            return None
        
        # 生成建议措施
        suggestions = self.get_suggestions(risk_score, prediction['factors'])
        
        return {
            'alert_id': f"alert_{datetime.now().strftime('%Y%m%d%H%M%S')}",
            'alert_level': alert_levels[risk_level],
            'risk_score': risk_score,
            'risk_level': risk_level,
            'alert_message': f"检测到{risk_level}，霉菌生长概率为{risk_score:.1%}",
            'suggested_action': suggestions,
            'alert_time': datetime.now().isoformat(),
            'expiry_time': (datetime.now() + timedelta(hours=6)).isoformat()
        }

    def get_suggestions(self, risk_score: float, factors: Dict) -> str:
        """
        根据风险等级和因子生成建议措施
        
        Args:
            risk_score: 风险分数
            factors: 各因子得分
            
        Returns:
            建议措施字符串
        """
        suggestions = []
        
        if risk_score >= 0.8:
            suggestions.append("立即启动除湿设备")
            suggestions.append("检查墙体是否有结露")
            suggestions.append("考虑使用防霉剂")
        elif risk_score >= 0.6:
            suggestions.append("开启排风扇增加通风")
            suggestions.append("适当降低室内湿度")
            suggestions.append("检查漏水点")
        elif risk_score >= 0.4:
            suggestions.append("保持正常通风")
            suggestions.append("监控湿度变化")
        
        # 针对具体因子给出建议
        if factors['humidity_score'] > 0.8:
            suggestions.append("湿度偏高，建议除湿")
        
        if factors['ventilation_factor'] > 0.8:
            suggestions.append("通风不足，建议开窗或开风扇")
            
        if factors['condensation_risk'] > 0.7:
            suggestions.append("结露风险高，建议提高墙体温度")
        
        return "；".join(suggestions)


# 测试代码
if __name__ == "__main__":
    # 创建模型实例
    model = MoldPredictionModel()
    
    print("=" * 60)
    print("霉菌生长概率预测模型测试")
    print("=" * 60)
    
    # 测试场景1：正常环境
    print("\n【测试场景1：正常家庭环境】")
    result1 = model.predict_mold_growth_probability(
        room_temp=22.5,
        room_humidity=55.0,
        weather='sunny',
        wall_material='paint',
        ventilation_rate=1.0,
        hour=14,
        season='spring'
    )
    print(f"霉菌生长概率: {result1['mold_risk_score']:.1%}")
    print(f"风险等级: {result1['risk_level']}")
    print(f"露点温度: {result1['environmental_data']['dew_point']:.1f}°C")
    
    # 测试场景2：高风险环境
    print("\n【测试场景2：高风险环境（浴室）】")
    result2 = model.predict_mold_growth_probability(
        room_temp=26.0,
        room_humidity=85.0,
        wall_temp=24.0,
        weather='rainy',
        wall_material='tile',
        ventilation_rate=0.2,
        hour=6,
        season='summer',
        days_since_cleaning=10
    )
    print(f"霉菌生长概率: {result2['mold_risk_score']:.1%}")
    print(f"风险等级: {result2['risk_level']}")
    print(f"结露风险: {result2['factors']['condensation_risk']:.1f}")
    
    # 生成预警
    alert = model.generate_alert(result2)
    if alert:
        print(f"\n预警信息: {alert['alert_message']}")
        print(f"建议措施: {alert['suggested_action']}")
    
    # 测试场景3：预测未来24小时
    print("\n【测试场景3：未来24小时预测】")
    current_data = {
        'room_temp': 24.0,
        'room_humidity': 70.0,
        'weather': 'cloudy',
        'wall_material': 'paint',
        'ventilation_rate': 0.5
    }
    
    forecasts = model.forecast_mold_risk(current_data, forecast_hours=6)  # 简化为6小时
    print("\n未来6小时预测：")
    for forecast in forecasts:
        print(f"{forecast['timestamp']}: 概率{forecast['mold_risk_score']:.1%}, "
              f"等级{forecast['risk_level']}, "
              f"温度{forecast['predicted_temp']:.1f}°C, "
              f"湿度{forecast['predicted_humidity']:.1f}%RH")
    
    print("\n" + "=" * 60)
    print("测试完成！")
    print("=" * 60)
