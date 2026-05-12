# 患者信息结果可视化与 Warfarin 个体化剂量输出 - 最终记录（2026-05-12）

## 1) 目的
- 明确“患者信息填写后，结果页面应体现作用”的最终输出形式。
- 汇总本次对个体化结果展示链路的优化内容（UI + 计算摘要 + PDF）。

---

## 2) 当前系统会输出什么结果（最终版）

### 2.1 页面可见输出
1. **Patient Clinical Profile（患者临床信息）**
   - 在结果页与“已保存结果页”顶部直接展示：年龄、身高、体重、性别。
   - 若样本缺少 profile，会显示“无患者信息”的提示。

2. **Personalized Warfarin Dose (PoC)**
   - 顶部新增独立区块，始终可见：
     - 计算后的 Warfarin 起始剂量（mg/week）
     - VKORC1 / CYP2C9 基因标记检测情况
     - 使用的基因惩罚项数值
     - 状态说明（是否已匹配到 Warfarin，或仅作为参考）
   - 即使 Warfarin 不是当前匹配药物，也会清晰提示“结果为参考值”。

3. **原有匹配结果**
   - 原有匹配标签列表保持不变，Warfarin 标签内部仍会显示剂量提醒。

### 2.2 PDF 输出
PDF 临床报告新增：
- 患者临床信息表（Age/Height/Weight/Gender）。
- Warfarin 个体化剂量摘要区块（与页面一致）。

---

## 3) 关键代码更新

### 3.1 新增数据结构
**`WarfarinDoseSummary`**
- 用于向 JSP 输出完整的剂量计算摘要。
- 字段包含：weeklyDose、formattedDose、基因标记、基因惩罚、状态说明等。

### 3.2 服务层增强
1. `DosageCalculatorService`
   - 新增 `buildWarfarinDoseSummary(...)`：统一生成 warfarin 个体化剂量摘要。
   - 抽取 profile 合法性校验与基因标记检测逻辑，避免散落在 UI 或 controller 内部。

### 3.3 控制器与前端展示
1. `MatchingController`
   - 结果页统一注入：
     - `patientProfile`
     - `warfarinDoseSummary`
   - Warfarin 是否匹配的状态也在 summary 中体现。

2. 结果页与 PDF
   - `matching_index_search.jsp`
   - `matching_result.jsp`
   - 新增患者信息展示区块。
   - 新增 Warfarin 个体化剂量摘要区块。
   - PDF 导出模板同步新增以上两块内容。

---

## 4) 主要涉及文件
- `src/main/java/cn/edu/zju/bean/WarfarinDoseSummary.java`
- `src/main/java/cn/edu/zju/service/DosageCalculatorService.java`
- `src/main/java/cn/edu/zju/controller/MatchingController.java`
- `src/main/webapp/views/matching_index_search.jsp`
- `src/main/webapp/views/matching_result.jsp`

---

## 5) 验证
- `mvn test`
- `mvn package -DskipTests`

---

## 6) 备注
- Warfarin 剂量计算仍为 PoC 公式，仅用于界面展示与流程验证。
