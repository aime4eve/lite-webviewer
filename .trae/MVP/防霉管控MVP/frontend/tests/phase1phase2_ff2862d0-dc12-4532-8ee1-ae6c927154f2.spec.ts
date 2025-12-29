
import { test } from '@playwright/test';
import { expect } from '@playwright/test';

test('Phase1Phase2_2025-12-24', async ({ page, context }) => {
  
    // 导航到设备注册页面
    await page.goto('http://localhost:6688/operator/device-registration');
    
    // 等待页面加载完成
    await page.waitForLoadState('networkidle');

    // 测试设备注册功能
    await test.step('设备注册', async () => {
      await page.click('.add-device-btn');
      await page.fill('#device-name', '测试温湿度传感器');
      await page.selectOption('#device-type', 'sensor');
      await page.fill('#device-sn', 'SN-TEST-001');
      await page.fill('#device-model', 'SMG-SENSOR-001');
      // 监听alert弹窗
      page.on('dialog', dialog => {
        console.log('Alert弹窗内容:', dialog.message());
        dialog.accept();
      });
      // 使用force点击避免元素不稳定问题
      await page.click('.confirm-btn', { force: true });
      await page.waitForTimeout(2000);
      // 搜索新添加的设备
      await page.fill('input[placeholder="搜索设备SN码或名称"]', '测试温湿度传感器');
      await page.waitForTimeout(1000);
      // 获取所有设备名称，包括表头
      const deviceNames = await page.locator('.device-name').allTextContents();
      console.log('设备名称列表:', deviceNames);
      // 检查是否包含新添加的设备名称
      expect(deviceNames.includes('测试温湿度传感器')).toBeTruthy();
    });

    // 中文支持截图
    const chineseText = await page.evaluate(() => {
      const element = document.querySelector('.device-name');
      return element ? element.textContent : '';
    });
    console.log('页面中文文本:', chineseText);
    await page.screenshot({ 
      path: '/home/agentic/uni-app-ui-demo/frontend/tests/screenshots/设备注册页面.png', 
      fullPage: true,
      type: 'png'
    });
});