# Precision Medicine Matching System — Data Design V3 更新记录

> 分支：`copilot/featuredata-design-optimization`  
> 适用版本：完成 `migration_v3_variant_partitioning.sql` 后  
> 目的：解释本次数据设计改造“改了什么、为什么改、对业务有什么帮助、为什么匹配结果可能变化”

---

## 1. 背景与目标

旧表 `annovar` 是典型宽表（154+列），在本系统里带来两个主要问题：

1. **热查询负担过重**：匹配流程实际只用少数字段（基因符号、ACMG分类、sample_id），但每次都在大宽表上查。
2. **扩展与维护困难**：DAO 插入语句超长，字段演进和排错成本很高。

本次改造目标是把“高频字段”和“低频长尾字段”分离，优化匹配路径性能，同时保持业务可用。

---

## 2. 技术栈确认（与实现方式强相关）

本仓库是：

- **数据库**：MySQL
- **数据访问方式**：原生 JDBC（`DriverManager + PreparedStatement`）
- **不是 ORM 项目**（无 JPA/Hibernate Entity 映射）

所以本次改造集中在 **SQL DDL + DAO SQL 重构**，而不是 ORM 实体映射迁移。

---

## 3. 本次数据库结构更新点（V3）

### 3.1 旧表处理

- 旧 `annovar` 重命名为 `annovar_legacy`（保留原始数据，方便回滚/核对）。

### 3.2 新增三张分层表

1. `variant_core`（核心定位信息）
    - `id` (PK, auto increment)
    - `sample_id` (FK -> sample.id)
    - `chr`, `start_pos`, `end_pos`, `ref_allele`, `alt_allele`

2. `variant_annotation`（匹配高频字段）
    - `variant_id` (PK, FK -> variant_core.id)
    - `gene_symbol`
    - `acmg_classification`

3. `variant_bio_details`（长尾字段 JSON）
    - `variant_id` (PK, FK -> variant_core.id)
    - `raw_details` (JSON)

### 3.3 级联删除

- 外键均带 `ON DELETE CASCADE`：
    - 删除 `sample` 后，会自动清理该 sample 对应的 `variant_core`
    - 再联动清理 `variant_annotation` 和 `variant_bio_details`

避免历史脏数据残留。

---

## 4. 索引策略更新（含你遇到的报错说明）

### 4.1 已保留索引

- `variant_core(sample_id)`：用于样本维度快速过滤。

### 4.2 你遇到的错误原因

你执行下面语句报错：

```sql
CREATE INDEX idx_variant_annotation_gene_symbol ON variant_annotation (gene_symbol);
```

报错 `Specified key was too long` 的原因是：

- `gene_symbol` 定义为 `VARCHAR(1024)`；
- 在常见 `utf8mb4` 下，一个字符最多 4 字节；
- 索引键长度可能超过 InnoDB 上限（通常 3072 bytes）。

### 4.3 解决方案（已在 migration_v3 中落地）

改为**前缀索引**，并做“存在性判断后再创建”：

```sql
CREATE INDEX idx_variant_annotation_gene_symbol
ON variant_annotation (gene_symbol(191));
```

191 的前缀在 utf8mb4 下是 764 bytes，安全。

> 通俗解释：  
> 你不用放弃这个优化，只要“索引前191个字符”就行。  
> 对基因符号这类通常很短的值，效果基本等同完整索引。

### 4.4 你当前“前面都跑完了，只差最后一步”如何补救

直接补跑这一句即可：

```sql
CREATE INDEX idx_variant_annotation_gene_symbol
ON variant_annotation (gene_symbol(191));
```

如果同名索引已存在会报重复名，再先 `DROP INDEX` 后重建。

---

## 5. DAO / 模型层变化

### 5.1 模型拆分（Composition）

新增：

- `VariantCore`
- `VariantAnnotation`
- `VariantBioDetails`

且 `VariantCore` 组合 `VariantAnnotation`（避免 154 字段巨型对象）。

### 5.2 DAO 查询优化

`AnnovarDao` 新增/重构了关键方法：

- `findAnnotationsBySampleId(int sampleId)`  
  只 JOIN `variant_core + variant_annotation`，显式列查询，不用 `SELECT *`
- `getBioDetails(int variantId)`  
  仅在前端需要详情时再查 `variant_bio_details`（懒加载）
- `findAnnotationsByGeneSymbolExact(String geneSymbol)`  
  精确匹配，避免 `UPPER()` 和前导 `%LIKE` 破坏索引

---

## 6. 为什么同一份数据匹配结果和以前不一样？

这是你最关心的问题，核心原因是**匹配输入基因集合构造逻辑变了**。

### 6.1 旧逻辑（annovar 宽表时代）

旧版 `getRefGenes` 使用 SQL（历史版本）：

```sql
select distinct `Gene.refGene`
from annovar
where `ExonicFunc.refGene` != 'synonymous SNV'
  and sample_id = ?
```

特点：

1. **显式排除了** `synonymous SNV`（同义突变）。
2. 直接取 `Gene.refGene` 字段，不做分词展开。

### 6.2 新逻辑（V3）

当前逻辑从 `variant_annotation.gene_symbol` 读取后，还会按 `,` / `;` 拆分基因符号并去重。

这会导致：

1. 基因集合可能比以前**更大**（拆分后命中更多）。
2. 目前不再直接通过 `ExonicFunc.refGene != 'synonymous SNV'` 过滤，可能引入以前被排除的基因。

因此“同一数据，匹配结果不同”是可预期现象。

> 通俗解释：  
> 以前像“先筛掉一批，再拿一个整字符串匹配”；  
> 现在像“不过滤那批了，还把字符串拆成多个基因逐个匹配”，命中自然会变化。

---

## 7. 这些变化对应用的帮助

### 7.1 性能收益（主收益）

1. **匹配主路径更轻**：高频查询只扫小表+索引，不扫宽表。
2. **I/O 减少**：不再反复读取 140+ 长尾列。
3. **可维护性提升**：DAO SQL 和模型结构更清晰。

### 7.2 架构收益

1. 高频字段和长尾字段解耦，后续可以分别优化。
2. `raw_details` JSON 可以承载格式演进，不必频繁改表。
3. 级联删除保证数据一致性。

---

## 8. 如果你希望“结果尽量和旧版一致”，建议

可以在新结构中补回“排除同义突变”的过滤策略（读取 `raw_details` 内对应列值，再过滤）。  
这样可以在保留新结构性能优势的同时，最大化贴近旧版业务语义。

---

## 9. 变更文件清单（V3 数据设计相关）

- `src/main/sql/migration_v3_variant_partitioning.sql`
- `src/main/java/cn/edu/zju/bean/VariantCore.java`
- `src/main/java/cn/edu/zju/bean/VariantAnnotation.java`
- `src/main/java/cn/edu/zju/bean/VariantBioDetails.java`
- `src/main/java/cn/edu/zju/dao/AnnovarDao.java`

