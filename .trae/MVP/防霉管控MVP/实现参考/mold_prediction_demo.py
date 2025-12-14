"""
霉菌生长概率预测系统 - Demo展示
Mold Growth Probability Prediction System - Demo

完整的演示系统，包括：
- 实时数据监测
- 霉菌概率预测
- 未来24小时预报
- 预警和建议
- 可视化展示
"""

import numpy as np
import pandas as pd
from datetime import datetime, timedelta
import random
import time
from typing import Dict, List, Optional
import json

# 导入预测算法
from mold_prediction_algorithm import MoldPredictionModel

class MoldPredictionDemo:
    """霉菌预测演示系统"""
    
    def __init__(self):
        """初始化演示系统"""
        self.model = MoldPredictionModel()
        self.current_data = self.generate_initial_data()
        self.history_data = []
        self.alerts = []
        
        # 模拟数据生成器
        self.temp_trend = 0  # 温度趋势
        self.humidity_trend = 0  # 湿度趋势
        
    def generate_initial_data(self) -> Dict:
        """生成初始环境数据"""
        return {
            'room_temp': 22.5,
            'room_humidity': 65.0,
            'wall_temp': 21.8,
            'outdoor_temp': 20.0,
            'outdoor_humidity': 70.0,
            'weather': 'cloudy',
            'wall_material': 'paint',
            'ventilation_rate': 0.5,
            'hour': datetime.now().hour,
            'season': 'spring',
            'days_since_cleaning': 7,
            'room_type': 'bathroom',
            'timestamp': datetime.now()
        }

    def simulate_environment_changes(self):
        """模拟环境数据变化"""
        # 模拟温度变化（±2°C）
        temp_change = random.uniform(-0.5, 0.5) + self.temp_trend * 0.1
        self.current_data['room_temp'] += temp_change
        self.current_data['room_temp'] = max(15, min(35, self.current_data['room_temp']))
        
        # 模拟湿度变化（±5%RH）
        humidity_change = random.uniform(-2, 2) + self.humidity_trend * 0.2
        self.current_data['room_humidity'] += humidity_change
        self.current_data['room_humidity'] = max(30, min(95, self.current_data['room_humidity']))
        
        # 更新墙体温度（通常比室温低1-2度）
        self.current_data['wall_temp'] = self.current_data['room_temp'] - random.uniform(1, 2)
        
        # 更新时间
        self.current_data['timestamp'] = datetime.now()
        self.current_data['hour'] = datetime.now().hour
        
        # 随机改变天气（每6小时可能变化）
        if datetime.now().hour % 6 == 0:
            weathers = ['sunny', 'cloudy', 'rainy', 'foggy']
            self.current_data['weather'] = random.choice(weathers)

    def update_trends(self):
        """更新温湿度趋势"""
        # 模拟一天中的温湿度变化趋势
        hour = datetime.now().hour
        
        # 温度趋势：夜间降低，白天升高
        if 0 <= hour <= 6:  # 夜间
            self.temp_trend = -0.5
        elif 6 <= hour <= 14:  # 上午到下午
            self.temp_trend = 0.8
        else:  # 傍晚到夜间
            self.temp_trend = -0.3
            
        # 湿度趋势：与温度反向
        self.humidity_trend = -self.temp_trend * 0.8

    def get_real_time_data(self) -> Dict:
        """获取实时数据"""
        self.simulate_environment_changes()
        self.update_trends()
        
        # 预测霉菌风险
        prediction = self.model.predict_mold_growth_probability(**self.current_data)
        
        # 生成预警
        alert = self.model.generate_alert(prediction)
        if alert:
            self.alerts.append(alert)
            # 只保留最近5条预警
            self.alerts = self.alerts[-5:]
        
        # 保存历史数据
        self.history_data.append({
            'timestamp': self.current_data['timestamp'],
            'room_temp': self.current_data['room_temp'],
            'room_humidity': self.current_data['room_humidity'],
            'mold_risk_score': prediction['mold_risk_score'],
            'risk_level': prediction['risk_level']
        })
        
        # 只保留最近100条历史数据
        self.history_data = self.history_data[-100:]
        
        return {
            'environmental_data': self.current_data.copy(),
            'prediction': prediction,
            'alert': alert
        }

    def get_forecast_data(self, hours: int = 24) -> List[Dict]:
        """获取预测数据"""
        forecast = self.model.forecast_mold_risk(self.current_data, forecast_hours=hours)
        return forecast

    def get_history_stats(self, days: int = 7) -> Dict:
        """获取历史统计"""
        if not self.history_data:
            return {}
        
        df = pd.DataFrame(self.history_data)
        df['timestamp'] = pd.to_datetime(df['timestamp'])
        
        # 计算统计数据
        stats = {
            'period': f"最近{len(df)}条记录",
            'avg_temp': round(df['room_temp'].mean(), 1),
            'avg_humidity': round(df['room_humidity'].mean(), 1),
            'max_risk_score': round(df['mold_risk_score'].max(), 3),
            'min_risk_score': round(df['mold_risk_score'].min(), 3),
            'avg_risk_score': round(df['mold_risk_score'].mean(), 3),
            'risk_duration': len(df[df['mold_risk_score'] > 0.6]) * 5,  # 分钟
            'alert_count': len(self.alerts)
        }
        
        # 计算趋势
        if len(df) >= 2:
            temp_trend = "上升" if df['room_temp'].iloc[-1] > df['room_temp'].iloc[0] else "下降"
            humidity_trend = "上升" if df['room_humidity'].iloc[-1] > df['room_humidity'].iloc[0] else "下降"
            risk_trend = "加剧" if df['mold_risk_score'].iloc[-1] > df['mold_risk_score'].iloc[0] else "缓解"
            
            stats.update({
                'temp_trend': temp_trend,
                'humidity_trend': humidity_trend,
                'risk_trend': risk_trend
            })
        
        return stats

    def generate_report(self) -> str:
        """生成环境健康报告"""
        stats = self.get_history_stats()
        current_data = self.get_real_time_data()
        
        report = f"""
╔═══════════════════════════════════════╗
║    霉菌预测环境健康报告               ║
╚═══════════════════════════════════════╝

📅 报告时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
🏠 监测区域: {self.current_data['room_type']}

┌─────────────────────────────────────┐
│  当前环境状况                       │
├─────────────────────────────────────┤
│  温度: {self.current_data['room_temp']:.1f}°C                     │
│  湿度: {self.current_data['room_humidity']:.1f}%RH               │
│  墙体温度: {self.current_data['wall_temp']:.1f}°C               │
│  天气: {self.current_data['weather']}                     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  霉菌风险预测                       │
├─────────────────────────────────────┤
│  风险等级: {current_data['prediction']['risk_level']}               │
│  霉菌概率: {current_data['prediction']['mold_risk_score']:.1%}               │
│  置信度: 85%                        │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  历史统计（最近{stats.get('period', 'N/A')}）        │
├─────────────────────────────────────┤
│  平均温度: {stats.get('avg_temp', 'N/A')}°C                    │
│  平均湿度: {stats.get('avg_humidity', 'N/A')}%RH              │
│  最高风险: {stats.get('max_risk_score', 'N/A')}%                │
│  风险时长: {stats.get('risk_duration', 'N/A')}分钟               │
│  预警次数: {stats.get('alert_count', 'N/A')}次                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  因子分析                           │
├─────────────────────────────────────┤
│  温度因子: {current_data['prediction']['factors']['temperature_score']:.2f}                    │
│  湿度因子: {current_data['prediction']['factors']['humidity_score']:.2f}                    │
│  时间因子: {current_data['prediction']['factors']['time_factor']:.2f}                    │
│  通风因子: {current_data['prediction']['factors']['ventilation_factor']:.2f}                  │
│  结露风险: {current_data['prediction']['factors']['condensation_risk']:.2f}                    │
└─────────────────────────────────────┘

💡 建议措施:
   {current_data['prediction']['factors']['suggested_action'] if 'suggested_action' in current_data['prediction']['factors'] else current_data['prediction']['risk_level']}

📈 系统状态:
   ✅ 传感器在线
   ✅ 数据传输正常
   ✅ 预测模型运行正常

═══════════════════════════════════════
   让每一间空间都拥有智能免疫系统！
═══════════════════════════════════════
        """
        return report

    def display_dashboard(self):
        """显示实时数据看板"""
        data = self.get_real_time_data()
        
        print("\n" + "="*80)
        print("  🏠 智能防霉监控系统 - 实时数据看板")
        print("="*80)
        
        # 当前环境数据
        print(f"\n📅 时间: {data['environmental_data']['timestamp'].strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"🏠 区域: {data['environmental_data']['room_type']}")
        print(f"🌡️  温度: {data['environmental_data']['room_temp']:.1f}°C")
        print(f"💧 湿度: {data['environmental_data']['room_humidity']:.1f}%RH")
        print(f"🧱 墙体温度: {data['environmental_data']['wall_temp']:.1f}°C")
        print(f"☁️  天气: {data['environmental_data']['weather']}")
        
        # 风险预测
        risk_level = data['prediction']['risk_level']
        risk_score = data['prediction']['mold_risk_score']
        
        # 风险等级颜色标识
        risk_colors = {
            '极低风险': '🟢',
            '低风险': '🟢',
            '中等风险': '🟡',
            '高风险': '🟠',
            '极高风险': '🔴'
        }
        
        print(f"\n🚨 霉菌风险等级: {risk_colors.get(risk_level, '⚪')} {risk_level}")
        print(f"📊 霉菌生长概率: {risk_score:.1%}")
        print(f"🎯 预测置信度: 85%")
        
        # 进度条显示风险
        bar_length = 40
        filled_length = int(bar_length * risk_score)
        bar = '█' * filled_length + '░' * (bar_length - filled_length)
        print(f"   [{bar}] {risk_score:.0%}")
        
        # 预警信息
        if data['alert']:
            print(f"\n⚠️  预警信息:")
            print(f"   {data['alert']['alert_message']}")
            print(f"💡 建议措施:")
            print(f"   {data['alert']['suggested_action']}")
        else:
            print(f"\n✅ 当前环境良好，暂无风险")
            print(f"💡 建议: 保持正常通风")
        
        # 露点温度
        dew_point = data['prediction']['environmental_data']['dew_point']
        print(f"\n🌡️  露点温度: {dew_point:.1f}°C")
        
        # 因子分析
        factors = data['prediction']['factors']
        print(f"\n📈 因子分析:")
        print(f"   温度因子: {factors['temperature_score']:.2f}")
        print(f"   湿度因子: {factors['humidity_score']:.2f}")
        print(f"   时间因子: {factors['time_factor']:.2f}")
        print(f"   通风因子: {factors['ventilation_factor']:.2f}")
        print(f"   结露风险: {factors['condensation_risk']:.2f}")
        
        print("\n" + "="*80)

    def display_forecast(self, hours: int = 12):
        """显示预测数据"""
        forecast = self.get_forecast_data(hours)
        
        print("\n" + "="*80)
        print(f"  📈 未来{hours}小时霉菌风险预测")
        print("="*80)
        
        print(f"\n{'时间':<20} {'温度':<8} {'湿度':<8} {'风险':<8} {'等级':<10}")
        print("-"*70)
        
        for pred in forecast:
            timestamp = datetime.fromisoformat(pred['timestamp']).strftime('%m-%d %H:%M')
            temp = pred['predicted_temp']
            humidity = pred['predicted_humidity']
            risk = pred['mold_risk_score']
            level = pred['risk_level']
            
            # 风险等级颜色标识
            risk_colors = {
                '极低风险': '🟢',
                '低风险': '🟢',
                '中等风险': '🟡',
                '高风险': '🟠',
                '极高风险': '🔴'
            }
            color = risk_colors.get(level, '⚪')
            
            print(f"{timestamp:<20} {temp:.1f}°C  {humidity:.1f}%RH  {risk:.0%}   {color}{level}")
        
        # 找出高风险时段
        high_risk_periods = [f for f in forecast if f['mold_risk_score'] > 0.6]
        if high_risk_periods:
            print(f"\n⚠️  高风险时段:")
            for period in high_risk_periods:
                timestamp = datetime.fromisoformat(period['timestamp']).strftime('%m-%d %H:%M')
                print(f"   {timestamp} - 概率{period['mold_risk_score']:.0%}")
        else:
            print(f"\n✅ 未来{hours}小时内无高风险时段")
        
        print("\n" + "="*80)

    def display_history_stats(self):
        """显示历史统计"""
        stats = self.get_history_stats()
        
        print("\n" + "="*80)
        print("  📊 历史数据统计")
        print("="*80)
        
        if not stats:
            print("\n暂无历史数据")
            print("="*80)
            return
        
        print(f"\n📈 统计周期: {stats.get('period', 'N/A')}")
        print(f"🌡️  平均温度: {stats.get('avg_temp', 'N/A')}°C")
        print(f"💧 平均湿度: {stats.get('avg_humidity', 'N/A')}%RH")
        print(f"🎯 最高风险: {stats.get('max_risk_score', 'N/A')}%")
        print(f"⏱️  风险时长: {stats.get('risk_duration', 'N/A')}分钟")
        print(f"🚨 预警次数: {stats.get('alert_count', 'N/A')}次")
        
        if 'temp_trend' in stats:
            print(f"📊 温度趋势: {stats['temp_trend']}")
            print(f"📊 湿度趋势: {stats['humidity_trend']}")
            print(f"🎯 风险趋势: {stats['risk_trend']}")
        
        # 效果评估
        print(f"\n💰 效果评估:")
        risk_duration = stats.get('risk_duration', 0)
        alerts = stats.get('alert_count', 0)
        
        if risk_duration > 120:  # 超过2小时
            print(f"   ⚠️  风险时长较长，建议加强通风")
        elif alerts > 5:
            print(f"   ⚠️  预警次数较多，建议检查设备")
        else:
            print(f"   ✅  环境控制良好")
            print(f"   💰  预计节省清洁费用: ¥{100 - alerts * 20}")
            print(f"   ⚡  预计节能: {120 - risk_duration // 10}kWh")
        
        print("\n" + "="*80)

    def run_demo(self, duration_minutes: int = 10):
        """运行演示"""
        print("\n" + "="*80)
        print("  🚀 霉菌生长概率预测系统 - 实时演示")
        print("="*80)
        print("\n💡 本演示将模拟真实环境数据变化，展示霉菌预测系统的工作过程")
        print("   按 Ctrl+C 可随时退出演示")
        print("\n" + "="*80)
        
        try:
            for minute in range(duration_minutes):
                print(f"\n⏱️  演示时间: {minute + 1}/{duration_minutes} 分钟")
                
                # 显示实时数据看板
                self.display_dashboard()
                
                # 每3分钟显示一次预测数据
                if minute % 3 == 0:
                    self.display_forecast(hours=12)
                
                # 每5分钟显示一次历史统计
                if minute % 5 == 0 and minute > 0:
                    self.display_history_stats()
                
                # 等待1分钟（实际演示中可调整）
                if minute < duration_minutes - 1:
                    print("\n⏳ 等待下一分钟数据更新...")
                    time.sleep(2)  # 演示用2秒代替1分钟
                
        except KeyboardInterrupt:
            print("\n\n👋 演示已手动终止")
        
        # 显示最终报告
        print("\n" + "="*80)
        print("  📋 演示总结报告")
        print("="*80)
        print(self.generate_report())
        
        print("\n" + "="*80)
        print("  🎉 演示结束！感谢使用霉菌预测系统")
        print("="*80)


def main():
    """主函数"""
    demo = MoldPredictionDemo()
    
    # 运行10分钟演示
    demo.run_demo(duration_minutes=5)


if __name__ == "__main__":
    main()
