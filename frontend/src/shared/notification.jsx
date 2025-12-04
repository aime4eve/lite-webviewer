import React from 'react';
import { Button, Popconfirm } from 'antd';

export const NOTIFY_KEYS = {
  treeLoaded: 'file-tree-loaded',
  treeLoadFail: 'file-tree-load-fail',
  scanSuccess: 'scan-success',
  scanFail: 'scan-fail',
};

export const computeStatsLabel = (paths) => {
  const arr = Array.isArray(paths) ? paths : [];
  const m = new Map();
  arr.forEach((p) => {
    const ext = (p.split('.').pop() || '').toLowerCase();
    const key = ['md', 'pdf', 'html', 'csv', 'svg'].includes(ext) ? ext : 'other';
    m.set(key, (m.get(key) || 0) + 1);
  });
  const order = ['md', 'pdf', 'html', 'csv', 'svg'];
  const label = { md: 'MD', pdf: 'PDF', html: 'HTML', csv: 'CSV', svg: 'SVG', other: '其他' };
  return order
    .filter((k) => m.get(k))
    .map((k) => `${label[k]} ${m.get(k)}`)
    .join(' · ');
};

export const notifyTreeLoaded = (notification, paths, opts = {}) => {
  const { onForceScan, loading } = opts;
  const arr = Array.isArray(paths) ? paths : [];
  const stats = computeStatsLabel(arr);
  const desc = arr.length === 0
    ? '未发现可预览文件'
    : stats
      ? `共加载 ${arr.length} 个文件（${stats}）`
      : `共加载 ${arr.length} 个文件`;

  const btn = arr.length === 0 && onForceScan
    ? (
        <Popconfirm
          title="确认强制扫描？"
          okText="执行"
          cancelText="取消"
          onConfirm={() => {
            notification.close(NOTIFY_KEYS.treeLoaded);
            onForceScan();
          }}
        >
          <Button type="link" size="small" loading={!!loading} disabled={!!loading}>
            强制扫描
          </Button>
        </Popconfirm>
      )
    : null;

  notification.success({ key: NOTIFY_KEYS.treeLoaded, message: '文件树已加载', description: desc, actions: btn });
};
