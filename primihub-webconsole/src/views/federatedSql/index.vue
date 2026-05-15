<template>
  <div class="federated-sql">
    <!-- 顶部标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2>联邦SQL查询</h2>
        <p class="desc">基于联邦SQL的多方数据联合查询，可对查询结果应用差分隐私技术进行脱敏处理</p>
      </div>
      <el-tag type="warning" effect="dark">FEDERATED + DP</el-tag>
    </div>

    <!-- 主区域 -->
    <div class="main-grid">
      <!-- 左侧：SQL编辑器 -->
      <div class="left-panel">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>SQL 查询</span>
            <div class="sample-links">
              <span class="sample-label">示例：</span>
              <el-button
                v-for="(q, i) in sampleQueries"
                :key="i"
                type="text"
                size="mini"
                @click="insertSample(i)"
              >Query {{ i + 1 }}</el-button>
            </div>
          </div>

          <el-input
            v-model="sql"
            type="textarea"
            :rows="6"
            placeholder="SELECT ... FROM __ALL__ WHERE ... GROUP BY ..."
            class="sql-editor"
          />

          <div v-if="error" class="error-msg">
            <i class="el-icon-warning" /> {{ error }}
          </div>

          <div class="action-bar">
            <div class="action-left">
              <el-button
                type="primary"
                :loading="status === 'running'"
                :disabled="!sql.trim() || selectedParties.length === 0"
                @click="executeQuery"
              >
                <i class="el-icon-video-play" /> 执行查询
              </el-button>
              <el-tag
                v-if="status === 'success'"
                type="success"
                size="small"
                effect="plain"
              >查询完成</el-tag>
              <el-tag
                v-if="status === 'error'"
                type="danger"
                size="small"
                effect="plain"
              >查询失败</el-tag>
            </div>
            <el-tag type="info" size="small">
              {{ selectedParties.length }} / {{ parties.length }} 参与方
            </el-tag>
          </div>
        </el-card>

        <!-- 结果区域 -->
        <el-card v-if="rawResult && displayResult" shadow="never" class="result-card">
          <div slot="header" class="card-header">
            <div class="result-controls">
              <span>查询结果</span>
              <el-radio-group
                v-if="dpEnabled"
                v-model="showDp"
                size="mini"
                class="dp-toggle"
              >
                <el-radio-button :label="true">
                  <i class="el-icon-shield" /> DP脱敏
                </el-radio-button>
                <el-radio-button :label="false">
                  <i class="el-icon-view" /> 原始
                </el-radio-button>
              </el-radio-group>
            </div>
            <el-tag size="small" type="info" effect="plain">{{ displayResult.rows.length }} 行</el-tag>
          </div>

          <el-table
            :data="displayResult.rows"
            border
            stripe
            size="small"
            class="result-table"
          >
            <el-table-column
              v-for="col in displayResult.columns"
              :key="col"
              :prop="col"
              :label="col"
              min-width="120"
            >
              <template slot-scope="{ row }">
                <span
                  :class="{
                    'dp-value': isDpValue(col, row),
                    'font-mono': typeof row[col] === 'number'
                  }"
                >
                  {{ formatValue(row[col]) }}
                  <el-tag
                    v-if="isDpValue(col, row)"
                    size="mini"
                    type="success"
                    class="dp-marker"
                  >DP</el-tag>
                </span>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="dpEnabled && showDp" class="dp-banner">
            <i class="el-icon-shield dp-banner-icon" />
            <div>
              <div class="dp-banner-title">(ε={{ epsilon }})-差分隐私已应用</div>
              <div class="dp-banner-desc">Laplace 机制噪声已注入数值列。结果满足 ε-差分隐私保证，敏感度 Δf={{ sensitivity }}。</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：参与方 + DP配置 -->
      <div class="right-panel">
        <el-card shadow="never">
          <div slot="header"><span>数据参与方</span></div>
          <div class="party-list">
            <div
              v-for="party in parties"
              :key="party.id"
              class="party-item"
              :class="{ selected: selectedParties.includes(party.id) }"
              @click="toggleParty(party.id)"
            >
              <el-checkbox
                :value="selectedParties.includes(party.id)"
                class="party-checkbox"
              />
              <span class="party-name">{{ party.name }}</span>
              <el-tag
                :type="party.connected ? 'success' : 'danger'"
                size="mini"
                effect="plain"
              >{{ party.connected ? '在线' : '离线' }}</el-tag>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="dp-card">
          <div slot="header" class="card-header">
            <span>差分隐私</span>
            <el-switch
              v-model="dpEnabled"
              active-color="#409EFF"
            />
          </div>

          <div :class="{ 'dp-disabled': !dpEnabled }">
            <div class="dp-field">
              <label>Epsilon (ε) <span class="dp-value-label">{{ epsilon }}</span></label>
              <el-slider
                v-model="epsilon"
                :min="0.01"
                :max="10"
                :step="0.01"
                :disabled="!dpEnabled"
                :format-tooltip="v => v.toFixed(2)"
              />
              <div class="slider-labels">
                <span>高隐私</span>
                <span>低隐私</span>
              </div>
            </div>

            <div class="dp-field">
              <label>敏感度 (Δf)</label>
              <el-select
                v-model="sensitivity"
                :disabled="!dpEnabled"
                size="small"
                class="full-width"
              >
                <el-option :value="0.1" label="0.1 (低)" />
                <el-option :value="0.5" label="0.5" />
                <el-option :value="1.0" label="1.0 (默认)" />
                <el-option :value="2.0" label="2.0" />
                <el-option :value="5.0" label="5.0 (高)" />
              </el-select>
            </div>

            <div v-if="dpEnabled && rawResult" class="dp-info">
              <i class="el-icon-success" style="color: #67C23A;" />
              ε={{ epsilon }}，Δf={{ sensitivity }} → 噪声尺度 = {{ (sensitivity / epsilon).toFixed(2) }}
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
const PARTIES = [
  { id: 'party-a', name: '参与方A (金融)', tables: [{ name: 'transactions', columns: ['tx_id', 'amount', 'currency', 'timestamp', 'user_id'] }, { name: 'users', columns: ['user_id', 'age', 'region', 'credit_score'] }], connected: true },
  { id: 'party-b', name: '参与方B (医疗)', tables: [{ name: 'claims', columns: ['claim_id', 'amount', 'diagnosis', 'provider', 'patient_id'] }, { name: 'patients', columns: ['patient_id', 'age', 'region', 'bmi'] }], connected: true },
  { id: 'party-c', name: '参与方C (零售)', tables: [{ name: 'orders', columns: ['order_id', 'total', 'category', 'customer_id', 'timestamp'] }, { name: 'customers', columns: ['customer_id', 'age', 'region', 'loyalty_score'] }], connected: true },
]

const SAMPLE_QUERIES = [
  'SELECT region, COUNT(*) as cnt, AVG(amount) as avg_amount FROM __ALL__ GROUP BY region',
  'SELECT age_group, AVG(amount) as avg_spend FROM __ALL__ GROUP BY age_group',
  'SELECT category, SUM(total) as revenue FROM __ALL__ GROUP BY category ORDER BY revenue DESC',
]

const MOCK_RESULTS = {
  region: {
    columns: ['region', 'cnt', 'avg_amount'],
    rows: [
      { region: 'North', cnt: 1247, avg_amount: 342.5 },
      { region: 'South', cnt: 982, avg_amount: 289.3 },
      { region: 'East', cnt: 1563, avg_amount: 415.8 },
      { region: 'West', cnt: 1104, avg_amount: 378.2 },
      { region: 'Central', cnt: 876, avg_amount: 256.7 },
    ]
  },
  age_group: {
    columns: ['age_group', 'avg_spend'],
    rows: [
      { age_group: '18-25', avg_spend: 215.4 },
      { age_group: '26-35', avg_spend: 389.7 },
      { age_group: '36-45', avg_spend: 452.1 },
      { age_group: '46-55', avg_spend: 398.6 },
      { age_group: '55+', avg_spend: 312.3 },
    ]
  },
  category: {
    columns: ['category', 'revenue'],
    rows: [
      { category: 'Electronics', revenue: 284500 },
      { category: 'Healthcare', revenue: 198200 },
      { category: 'Food & Beverage', revenue: 156800 },
      { category: 'Apparel', revenue: 124500 },
      { category: 'Services', revenue: 98200 },
    ]
  }
}

function laplaceNoise(value, epsilon, sensitivity) {
  if (epsilon <= 0) return value
  const scale = sensitivity / epsilon
  const u = Math.random() - 0.5
  const noise = -scale * Math.sign(u) * Math.log(1 - 2 * Math.abs(u))
  return Math.round((value + noise) * 100) / 100
}

function dpSanitize(result, epsilon, sensitivity) {
  const numericCols = result.columns.filter(col =>
    result.rows.some(r => typeof r[col] === 'number')
  )
  return {
    columns: result.columns,
    rows: result.rows.map(row => {
      const dpRow = { ...row }
      for (const col of numericCols) {
        if (typeof dpRow[col] === 'number') {
          dpRow[col] = laplaceNoise(dpRow[col], epsilon, sensitivity)
        }
      }
      return dpRow
    })
  }
}

function matchResult(sql) {
  const lower = sql.toLowerCase()
  for (const key of Object.keys(MOCK_RESULTS)) {
    if (lower.includes(key.toLowerCase())) {
      return JSON.parse(JSON.stringify(MOCK_RESULTS[key]))
    }
  }
  return JSON.parse(JSON.stringify(MOCK_RESULTS.region))
}

export default {
  name: 'FederatedSql',
  data() {
    return {
      parties: PARTIES,
      sampleQueries: SAMPLE_QUERIES,
      sql: '',
      selectedParties: ['party-a', 'party-b', 'party-c'],
      epsilon: 1.0,
      sensitivity: 1.0,
      dpEnabled: true,
      showDp: true,
      status: 'idle',
      rawResult: null,
      dpResult: null,
      error: null,
    }
  },
  computed: {
    displayResult() {
      if (!this.rawResult) return null
      if (this.dpEnabled && this.showDp && this.dpResult) {
        return this.dpResult
      }
      return this.rawResult
    }
  },
  methods: {
    insertSample(index) {
      this.sql = SAMPLE_QUERIES[index] || ''
    },
    toggleParty(id) {
      const idx = this.selectedParties.indexOf(id)
      if (idx >= 0) {
        this.selectedParties.splice(idx, 1)
      } else {
        this.selectedParties.push(id)
      }
    },
    formatValue(val) {
      if (typeof val === 'number') {
        return val.toLocaleString()
      }
      return val ?? ''
    },
    isDpValue(col, row) {
      if (!this.dpEnabled || !this.showDp || !this.dpResult || !this.rawResult) return false
      const rawRow = this.rawResult.rows[this.displayResult.rows.indexOf(row)]
      if (!rawRow) return false
      return typeof row[col] === 'number' && rawRow[col] !== row[col]
    },
    async executeQuery() {
      this.status = 'running'
      this.error = null
      this.rawResult = null
      this.dpResult = null

      await new Promise(r => setTimeout(r, 1500 + Math.random() * 1000))

      try {
        this.rawResult = matchResult(this.sql)
        this.dpResult = this.dpEnabled
          ? dpSanitize(this.rawResult, this.epsilon, this.sensitivity)
          : null
        this.status = 'success'
      } catch (e) {
        this.error = e.message || '查询执行失败'
        this.status = 'error'
      }
    }
  },
  watch: {
    dpEnabled(val) {
      if (!val) this.showDp = false
      else this.showDp = true
    },
    epsilon() {
      if (this.rawResult && this.dpEnabled) {
        this.dpResult = dpSanitize(this.rawResult, this.epsilon, this.sensitivity)
      }
    },
    sensitivity() {
      if (this.rawResult && this.dpEnabled) {
        this.dpResult = dpSanitize(this.rawResult, this.epsilon, this.sensitivity)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.federated-sql {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .desc {
    margin: 4px 0 0;
    color: #909399;
    font-size: 13px;
    max-width: 500px;
  }
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  align-items: start;
}

@media (max-width: 1000px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sample-links {
  display: flex;
  align-items: center;
  gap: 4px;

  .sample-label {
    font-size: 12px;
    color: #909399;
  }
}

.sql-editor {
  :deep(textarea) {
    font-family: 'Courier New', Courier, monospace;
    font-size: 13px;
    line-height: 1.5;
  }
}

.error-msg {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  font-size: 13px;
}

.action-bar {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-card {
  margin-top: 0;
}

.result-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-table {
  :deep(.dp-value) {
    color: #67C23A;
    font-weight: 600;
  }

  :deep(.font-mono) {
    font-family: 'Courier New', Courier, monospace;
    letter-spacing: 0.5px;
  }
}

.dp-marker {
  margin-left: 2px;
  transform: scale(0.7);
  vertical-align: super;
}

.dp-banner {
  margin-top: 12px;
  padding: 12px 16px;
  background: #f0f9eb;
  border-radius: 4px;
  display: flex;
  gap: 10px;
  align-items: flex-start;

  .dp-banner-icon {
    font-size: 20px;
    color: #67C23A;
    margin-top: 2px;
  }

  .dp-banner-title {
    color: #67C23A;
    font-weight: 600;
    font-size: 13px;
  }

  .dp-banner-desc {
    color: #909399;
    font-size: 12px;
    margin-top: 2px;
  }
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.party-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.party-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f5f7fa;
  }

  &.selected {
    border-color: #409EFF;
    background: #ecf5ff;
  }

  .party-name {
    flex: 1;
    font-size: 14px;
  }
}

.dp-card {
  .card-header {
    span {
      font-size: 14px;
      font-weight: 600;
    }
  }
}

.dp-disabled {
  opacity: 0.5;
  pointer-events: none;
}

.dp-field {
  margin-bottom: 16px;

  label {
    display: block;
    font-size: 13px;
    color: #606266;
    margin-bottom: 6px;

    .dp-value-label {
      float: right;
      color: #409EFF;
      font-family: monospace;
    }
  }

  .slider-labels {
    display: flex;
    justify-content: space-between;
    font-size: 11px;
    color: #909399;
    margin-top: -4px;
  }
}

.full-width {
  width: 100%;
}

.dp-info {
  margin-top: 12px;
  padding: 6px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
