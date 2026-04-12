# Precision Matching & Clearer Display — Coding & Implementation Record

## 1. 修改背景与目标

本次修改对应“Coding & Implementation”阶段中的 **Precision Matching & Clearer Display** 功能目标，重点完成：

1. 匹配引擎从旧的字符串 `.contains()` 命中逻辑升级为更稳健的基因 token 精确交集逻辑  
2. 在 Java 侧显式过滤良性变异（尤其 `synonymous SNV`）避免误匹配  
3. 保持 V3 三表结构 (`variant_core`, `variant_annotation`, `variant_bio_details`) 的分层职责，并延续懒加载思路  
4. 强化 MVC 输出，确保结果按分数降序并在 JSP 中按 score/recommendationLevel 条件渲染  
5. 保留并加固匹配结果快照写入 `matching_result` 的审计能力

---

## 2. 与三表结构的对齐确认

- `variant_core`：定位与样本维度核心字段  
- `variant_annotation`：匹配高频字段（gene_symbol, acmg_classification）  
- `variant_bio_details`：长尾 JSON 详情（按需加载）

本次匹配主查询仍然仅使用 `variant_core + variant_annotation` 的 JOIN 且显式列查询，不使用 `SELECT *`；  
仅在判定“是否为同义突变需过滤”时才按需读取 `variant_bio_details`，符合懒加载思想。

---

## 3. 代码改动清单与目的

### 3.1 `MatchingController.java`

**目的：** 重构匹配与评分逻辑，加入防御性空值处理和良性变异过滤。

关键改动：

- `matching()`：
  - 由 `getRefGenes(sampleId)` 改为 `findAnnotationsBySampleId(sampleId)` 获取结构化变异
  - 调用 `collectPatientGenesExcludingBenignVariants(...)` 生成过滤后的患者基因集合
  - 若过滤后为空，直接返回样本页，避免无意义匹配
- `doMatch(...)`：
  - 移除旧的 `summary.contains(gene)` 逻辑
  - 改为“药物标签 summary token 集合”与“患者基因集合”做交集命中
- 新增 `calculateScore(DrugLabel, Set<String> patientGenes)`：
  - 证据评分规则：1A=10，1B=8，2A=5，2B=3，Level 3=1
  - 若 `dosingInformation=true`，额外加 4 分
- 新增 benign 过滤：
  - `isBenignSynonymousVariant(VariantCore)` 中按需读取 `variant_bio_details`
  - 解析 JSON 中 `annovar_col_9`（ExonicFunc.refGene）包含 `synonymous snv` 时排除
- 防御性处理：
  - `null/blank` 检查、`Optional`、空集合短路、JSON 解析异常保护

---

### 3.2 `VariantCore.java`

**目的：** 强化 Composition 表达，支持详情懒加载挂载。

改动：

- 新增 `VariantBioDetails bioDetails` 字段及 getter/setter  
- 保留 `VariantAnnotation annotation` 组合关系

---

### 3.3 `AnnovarDao.java`

**目的：** 提供更清晰的懒加载入口，不破坏现有分层查询策略。

改动：

- 新增 `loadBioDetailsIfNeeded(VariantCore core)`：
  - 若 `bioDetails` 为空，则按 `variant_id` 读取并回填
  - 避免主查询强制 JOIN 长尾表，保持按需查询

---

### 3.4 `MatchingResultDao.java`

**目的：** 加固匹配快照读写可靠性与健壮性。

改动：

- `saveResults(...)`：
  - 空结果列表保护
  - 跳过无效 label（null/空 id）
  - `matchedGenes` 空安全处理
  - 使用事务 + rollback 容错
  - 使用 try-with-resources 规范化 `PreparedStatement`
- `findBySampleId(...) / hasResults(...) / findSampleIdsWithResults()`：
  - 全部改为 try-with-resources
  - 保持 PreparedStatement 防 SQL 注入
  - 明确日志语义，便于定位问题

---

### 3.5 JSP 视图更新

文件：

- `src/main/webapp/views/matching_index_search.jsp`
- `src/main/webapp/views/matching_result.jsp`

**目的：** 实现“更清晰显示”：按 score/recommendationLevel 条件化呈现。

改动：

- Accordion 卡片边框颜色按 score 分档：
  - `>=8` 绿色，`>=4` 黄色，其余灰色
- Score Badge 颜色按 score 分档（与上同）
- Recommendation Badge 增加空值兜底：`Unrated`
- 高分项（或首项）默认展开，提高可读性与决策优先级

---

## 4. SQL 注入与安全性说明

- 本次新增/修改 DAO 逻辑全部使用 `PreparedStatement`
- 未引入字符串拼接参数的动态 SQL
- 保持现有 JDBC 路径，不引入新依赖
- 对 JSON 解析失败采用安全降级，不中断主流程

---

## 5. 构建与验证

执行命令（本地仓库）：

- `mvn test`
- `mvn package -DskipTests`

结果：均通过（`BUILD SUCCESS`）。

---

## 6. 建议手工集成测试（3-4项）

1. **空/无效上传测试**  
   上传空 TSV 或格式错误 TSV，确认进入错误页且不会写入异常匹配结果。

2. **同义突变过滤验证**  
   准备仅包含 `ExonicFunc.refGene = synonymous SNV` 的样本，执行匹配，确认不会因该变异产生命中药物。

3. **评分与排序验证**  
   选择能命中不同证据等级的样本，确认分数符合规则（1A/1B/2A/2B/Level3 + FDA dosing）且按分数降序展示。

4. **审计快照与级联行为验证**  
   先完成一次匹配并查看 `matching_result` 写入；随后删除对应 sample，确认 V3 三表级联清理正常，且历史结果访问行为符合预期。

---

## 7. 本次修改文件清单

- `src/main/java/cn/edu/zju/controller/MatchingController.java`
- `src/main/java/cn/edu/zju/bean/VariantCore.java`
- `src/main/java/cn/edu/zju/dao/AnnovarDao.java`
- `src/main/java/cn/edu/zju/dao/MatchingResultDao.java`
- `src/main/webapp/views/matching_index_search.jsp`
- `src/main/webapp/views/matching_result.jsp`
- `PRECISION_MATCHING_IMPLEMENTATION_RECORD_2026-04-12.md`
