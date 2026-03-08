# 智慧垃圾清运系统 UI 设计规范

## 1. 视觉风格 (Visual Style)
- **核心理念**: 简洁、大气、科技、环保
- **配色方案**:
  - **主色调 (Primary)**: `#00C853` (鲜艳绿) - 用于按钮、高亮、图标
  - **次要色 (Secondary)**: `#4CAF50` (沉稳绿) - 用于渐变、辅助背景
  - **背景色 (Background)**: `#F5F7FA` (浅灰) - 页面底色
  - **卡片色 (Surface)**: `#FFFFFF` (纯白) - 内容承载
  - **文字色 (Text)**:
    - 主要: `#333333`
    - 次要: `#666666`
    - 提示: `#999999`
  - **边框色 (Border)**: `#E0E0E0`

## 2. 排版系统 (Typography)
- **字体**: "PingFang SC", "Microsoft YaHei", "Roboto", sans-serif
- **字号**:
  - H1: 24px (页面标题)
  - H2: 20px (模块标题)
  - H3: 16px (卡片标题)
  - Body: 14px (正文)
  - Small: 12px (辅助文字)

## 3. 布局规范 (Layout)
- **栅格系统**: 1200px 最大宽度，居中对齐
- **间距 (Spacing)**: 4px 为基数 (4, 8, 16, 24, 32px)
- **圆角 (Radius)**:
  - 按钮/输入框: 4px
  - 卡片: 12px
  - 模态框: 16px

## 4. 组件样式 (Components)
- **按钮 (Buttons)**:
  - 扁平化，移除浮雕效果
  - Hover 时增加阴影和亮度
  - Active 时有按压效果
- **卡片 (Cards)**:
  - 轻微阴影: `0 2px 12px 0 rgba(0,0,0,0.05)`
  - Hover 时悬浮效果: `0 8px 24px 0 rgba(0,0,0,0.1)`
- **表单 (Forms)**:
  - 高度 40px
  - 聚焦时绿色边框 `#00C853`，发光效果

## 5. 动画 (Animation)
- **过渡**: `all 0.3s ease-in-out`
- **场景**: 按钮 Hover、路由切换、模态框弹出
