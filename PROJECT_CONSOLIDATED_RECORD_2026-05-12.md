# 精准医疗匹配系统 — 整合修订记录（至 2026-05-12）

## 1) 初始基础框架（起点）
- Java Web 应用（JSP + Servlet），自定义 `DispatchServlet` 简单路由分发。
- MVC 分层：Controller / Service / DAO / Bean。
- 数据库：MySQL，JDBC 直连（`DriverManager + PreparedStatement`）。
- 知识库爬虫：对 PharmGKB 拉取 Drug / DrugLabel / Dosing Guideline 入库。
- 核心页面：Dashboard、Matching 上传与结果页、Samples 列表、知识库浏览页。

## 2) 从 0 开始累计改动（按阶段汇总）
### 2.1 UI/UX 升级（2026-03-24）
- Dashboard 补全统计卡片与近期活动表。
- 数据库表视图全面引入 DataTables（搜索 / 分页 / 排序）。
- 匹配结果页面改为 Accordion + Badge 结构，按评分排序。
- 新增匹配历史表 `matching_result`，支持结果回看与审计。

### 2.2 临床 UX 修复与一致性增强（2026-04-12）
- V3 结构整合：`variant_core` / `variant_annotation` / `variant_bio_details` 联合查询路径落地。
- Dashboard 增加药物与指南统计。
- Matching 上传页加入流程引导说明。
- Samples 页排序稳定化，动作统一为 “View Report”。
- 业务库 Summary 文本取消截断，支持换行展示。

### 2.3 精准匹配与结果清晰化（2026-04-12）
- 匹配逻辑改为基因 token 精确交集命中。
- 过滤良性同义突变（按需读取 `variant_bio_details`）。
- 评分与推荐等级统一：1A/1B/2A/2B/Level3 + FDA dosing 加权。
- 匹配结果保存与回读逻辑加固（空值、事务、容错）。
- 结果页按分数分档渲染样式。

### 2.4 PDF 导出链路重构（2026-04-27）
- 切换为同文档离屏导出容器。
- 新增导出前预览 Modal。
- 导出状态锁定、清理与防重入控制。

### 2.5 患者信息与 Warfarin 个体化剂量（2026-04-27 ~ 2026-05-12）
- 新增 `patient_profile` 表与 DAO。
- 上传时采集患者临床信息并持久化。
- 引入 `DosageCalculatorService` 统一计算 PoC 剂量与基因惩罚。
- 结果页与 PDF 同步展示患者信息与 Warfarin 剂量摘要。

## 3) 当前可用功能与核心逻辑（最终态）
### 3.1 业务流程
- 上传 ANNOVAR TSV → 入库变异三表 → 过滤良性突变 → 基因集合构造 → 药物标签匹配。
- 匹配结果按评分降序输出，推荐等级分档展示。
- 匹配结果保存到 `matching_result`，支持历史回看与审计。

### 3.2 结果展示
- Dashboard：样本数、变异数、药物数、指南数、近期活动。
- 结果页：药物匹配列表、评分/推荐等级、命中基因。
- 患者信息与 Warfarin 剂量摘要（无匹配时提示参考性质）。
- PDF 导出：预览 + 同文档导出，包含患者信息与剂量摘要。

### 3.3 知识库与数据浏览
- Drugs / Drug Labels / Dosing Guidelines 统一 DataTables 体验。
- Samples 历史按时间排序，直接查看报告。

## 4) 数据库结构要点（当前版本）
- `matching_result`：匹配快照。
- `variant_core / variant_annotation / variant_bio_details`：V3 分层结构。
- `patient_profile`：患者临床信息（1:1 关联 sample）。

---
*文档维护：Copilot Agent | 更新：2026-05-12*
