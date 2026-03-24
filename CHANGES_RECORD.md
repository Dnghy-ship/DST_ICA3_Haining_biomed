# 精准医疗匹配系统 - UI/UX 升级修改记录

**分支：** `feature/clinical-ux-upgrade`  
**修改日期：** 2026-03-24  
**修改依据：** DCY-Requirement_analysis.docx

---

## 一、修改背景

本次升级针对精准医疗匹配系统（Precision Medicine Matching System）的四个主要问题进行改进：

1. 仪表盘（Dashboard）空白，缺乏统计信息
2. 数据库表（药物、变异）未分页，缺乏搜索功能
3. 匹配结果页面冗长、难以阅读
4. 无匹配历史记录，缺乏审计追踪功能

---

## 二、Task 1：仪表盘与导航增强

### 涉及文件

| 文件 | 类型 | 操作 |
|------|------|------|
| `src/main/java/cn/edu/zju/bean/DashboardStats.java` | Java Bean | **新增** |
| `src/main/java/cn/edu/zju/service/DashboardService.java` | Service 层 | **新增** |
| `src/main/java/cn/edu/zju/dao/SampleDao.java` | DAO | **修改** |
| `src/main/java/cn/edu/zju/dao/AnnovarDao.java` | DAO | **修改** |
| `src/main/java/cn/edu/zju/controller/IndexController.java` | Controller | **修改** |
| `src/main/webapp/views/index.jsp` | JSP 前端 | **修改** |

### 具体修改内容

- **新增 `DashboardStats.java`**：用于封装统计数据（样本总数、变异总数、最近活动列表）
- **新增 `DashboardService.java`**：实现 `getDashboardStats()` 方法，统计并返回系统关键指标
- **`SampleDao.java` 新增方法**：
  - `count()`：统计样本总数
  - `findRecent(int limit)`：查询最近 N 条样本记录
- **`AnnovarDao.java` 新增方法**：
  - `countAll()`：统计变异记录总数
- **`IndexController.java`**：在转发至 `index.jsp` 前调用 `DashboardService.getDashboardStats()`，将统计数据放入请求属性
- **`index.jsp`**：
  - 原来只有一行欢迎文字，现在展示三张 Bootstrap 统计卡片（样本总数、变异总数、快捷操作）
  - 新增「近期匹配活动」表格，显示最近 5 条样本记录

---

## 三、Task 2：数据可用性（分页与筛选）

### 涉及文件

| 文件 | 类型 | 操作 |
|------|------|------|
| `src/main/webapp/views/drugs.jsp` | JSP 前端 | **修改** |
| `src/main/webapp/views/drug_labels.jsp` | JSP 前端 | **修改** |
| `src/main/webapp/views/dosing_guideline.jsp` | JSP 前端 | **修改** |
| `src/main/webapp/views/samples.jsp` | JSP 前端 | **修改** |

### 具体修改内容

- 所有数据表格引入 **DataTables 1.13.6**（通过 CDN，兼容 Bootstrap 4）
- 每个页面均添加：
  - 全局搜索框（DataTables 内置）
  - 分页控件（默认每页 25 条）
  - 列头排序功能
  - 行数统计展示（`${list.size()} total`）
- **`drugs.jsp`**：新增「Drug Name」列，优化 Biomarker 字段显示为绿色/灰色 Badge
- **`drug_labels.jsp`**：新增「Drug Name」列；摘要列（Summary）设置最大宽度并截断显示，鼠标悬停可见完整内容
- **`dosing_guideline.jsp`**：推荐（Recommendation）字段显示为 Badge；摘要列截断显示
- **`samples.jsp`**：添加「Upload New Sample」快捷按钮；DataTables 默认按 ID 倒序

---

## 四、Task 3：决策清晰度（匹配结果重设计）

### 涉及文件

| 文件 | 类型 | 操作 |
|------|------|------|
| `src/main/java/cn/edu/zju/bean/MatchedDrugLabel.java` | Java Bean | **新增** |
| `src/main/java/cn/edu/zju/controller/MatchingController.java` | Controller | **修改** |
| `src/main/webapp/views/matching_index_search.jsp` | JSP 前端 | **修改** |

### 具体修改内容

- **新增 `MatchedDrugLabel.java`**：继承 `DrugLabel`，扩展字段：
  - `score`：证据评分（整数）
  - `recommendationLevel`：推荐等级（Strong / Moderate / Optional）
  - `matchedGenes`：本次匹配命中的基因列表
- **`MatchingController` 匹配逻辑重构**：
  - `doMatch()` 方法：不再简单返回布尔值，改为收集每条药物标签的命中基因列表
  - 新增 `calculateScore(DrugLabel)` 方法：根据摘要中的证据等级代码（1A=10分、1B=8分、2A=5分、2B=3分、Level 3=1分）计算得分；若标签含 FDA 用药剂量信息默认 4 分
  - 新增 `getRecommendationLevel(int score)` 方法：≥8分→Strong，≥4分→Moderate，其余→Optional
  - 匹配结果按评分**降序排列**后返回前端
- **`matching_index_search.jsp`** 重新设计：
  - 弃用原有简单表格
  - 采用 **Bootstrap Accordion（折叠面板）** 展示每个匹配结果
  - 默认仅显示药物名称、命中基因（红色 Badge）、评分（蓝色 Badge）和推荐等级（成功/警告/灰色 Badge）
  - 展开后显示详细信息（来源、是否含剂量信息、摘要，摘要有高度限制并可滚动）
  - 第一条结果默认展开

---

## 五、Task 4：审计追踪（匹配历史记录）

### 涉及文件

| 文件 | 类型 | 操作 |
|------|------|------|
| `src/main/sql/migration_v2.sql` | SQL | **新增** |
| `src/main/java/cn/edu/zju/bean/MatchingResult.java` | Java Bean | **新增** |
| `src/main/java/cn/edu/zju/dao/MatchingResultDao.java` | DAO | **新增** |
| `src/main/java/cn/edu/zju/controller/MatchingController.java` | Controller | **修改** |
| `src/main/webapp/views/samples.jsp` | JSP 前端 | **修改** |
| `src/main/webapp/views/matching_result.jsp` | JSP 前端 | **新增** |

### 具体修改内容

- **`migration_v2.sql`**：新建 `matching_result` 数据表，字段包括：
  - `sample_id`（关联 sample 表）
  - `drug_label_id`（关联 drug_label 表）
  - `score`、`recommendation_level`、`matched_genes`（匹配信息）
  - `created_at`（记录时间）
  - > ⚠️ **注意**：需在 MySQL 客户端手动执行该脚本后，保存功能方可正常使用

- **`MatchingResult.java`**：对应 `matching_result` 数据表的实体类

- **`MatchingResultDao.java`** 新增方法：
  - `saveResults(int sampleId, List<MatchedDrugLabel>)`：保存/覆盖某样本的匹配结果
  - `findBySampleId(int sampleId)`：读取某样本已保存的匹配结果（JOIN drug_label 表还原完整信息）
  - `hasResults(int sampleId)`：判断样本是否有历史匹配结果
  - `findSampleIdsWithResults()`：返回所有有历史记录的样本 ID 集合

- **`MatchingController` 新增逻辑**：
  - `matching()` 方法：执行匹配后调用 `matchingResultDao.saveResults()` 存储结果（数据库异常不影响主流程）
  - 新增 `viewMatchingResult()` 方法：处理 `GET /matchingResult?sampleId=X`，若无历史记录则自动跳转重新匹配
  - `samples()` 方法：查询有历史记录的样本 ID 集合并传入视图

- **`samples.jsp`**：若样本存在历史记录，在操作列额外显示 **「View Result」（蓝色按钮）**，链接至 `/matchingResult?sampleId=X`

- **`matching_result.jsp`**（新建）：与匹配结果页面（`matching_index_search.jsp`）布局相同，使用 Accordion + Badge 展示已保存结果，并提供「Re-run Matching」和「Back to Samples」按钮

---

## 六、数据库变更说明

> **请手动在 MySQL 中执行以下 SQL：**

```sql
-- 文件位置：src/main/sql/migration_v2.sql
CREATE TABLE IF NOT EXISTS matching_result
(
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    sample_id            INT          NOT NULL,
    drug_label_id        VARCHAR(100) NOT NULL,
    score                INT          DEFAULT 0,
    recommendation_level VARCHAR(20),
    matched_genes        TEXT,
    created_at           DATETIME     NOT NULL,
    INDEX idx_matching_result_sample (sample_id)
);
```

---

## 七、证据评分规则说明

| summaryMarkdown 中包含的文本 | 得分 |
|-----------------------------|------|
| `1A` / `Level 1A` | 10 |
| `1B` / `Level 1B` | 8 |
| `2A` / `Level 2A` | 5 |
| `2B` / `Level 2B` | 3 |
| `Level 3` | 1 |
| 标签含 FDA 剂量信息（dosingInformation=true） | 4 |
| 无匹配 | 1 |

| 得分区间 | 推荐等级 | Badge 颜色 |
|---------|---------|-----------|
| ≥ 8 | Strong（强推荐） | 绿色 `badge-success` |
| 4 ~ 7 | Moderate（中等推荐） | 黄色 `badge-warning` |
| < 4 | Optional（可选） | 灰色 `badge-secondary` |

---

## 八、依赖说明

本次修改引入了 **DataTables 1.13.6**（CDN 方式加载），需要在应用运行时访问以下外部资源：

```
https://cdn.datatables.net/1.13.6/css/dataTables.bootstrap4.min.css
https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js
https://cdn.datatables.net/1.13.6/js/dataTables.bootstrap4.min.js
```

如在离线环境部署，请将上述文件下载后放入 `src/main/webapp/static/datatables/` 目录，并修改相应 JSP 中的引用路径。

---

*文档维护：Copilot Agent | 修改时间：2026-03-24*
