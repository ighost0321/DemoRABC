# DemoRABC 軟體規格書

- **版本**：1.0  
- **最後更新**：2025-10-13  
- **作者**：Ryan

---

## 1. 專案概述

DemoRABC 為一套基於 Spring Boot 的權限與功能管理系統，提供角色維護、功能維護、活動稽核與安全性登入（含驗證碼）等功能，可作為企業內部 RBAC（Role-Based Access Control）範例實作。

---

## 2. 系統目標

- 建立集中式權限管理介面，讓系統管理員維護角色與功能權限。  
- 提供登入稽核與操作紀錄，以符合內控制度。  
- 支援基於角色的功能授權，並於前端側邊欄展示使用者可存取之功能。  
- 強化登入安全性，阻擋暴力攻擊與機器人登入。

---

## 3. 系統範圍

| 模組 | 說明 |
| --- | --- |
| 使用者認證 | 表單登入、驗證碼、登入失敗鎖定、成功/失敗事件紀錄。 |
| 角色管理 | 角色查詢、編輯、功能關聯設定。 |
| 功能管理 | 功能查詢、更新、新增（含功能群組）。 |
| 活動稽核 | 操作紀錄查詢、篩選、分頁、統計頁面。 |
| 導覽展示 | Welcome 首頁與側邊欄功能群組動態載入。 |

---

## 4. 利害關係人與使用者角色

| 角色 | 描述 | 權限範圍 |
| --- | --- | --- |
| 系統管理員 | 管理角色、功能、檢視稽核紀錄。 | 全模組可存取。 |
| 審計人員 | 查閱活動紀錄、登入狀態。 | 限定查詢頁面。 |
| 一般使用者 | 使用被授權功能、瀏覽歡迎頁。 | 依角色授權限定。 |

---

## 5. 名詞定義

- **角色（Role）**：可授權給使用者的一組功能集合。  
- **功能（Function）**：對應系統實際網址/功能項目，可被角色引用。  
- **功能群組（Function Group）**：將功能以類別分組，供 UI 展示。  
- **活動日誌（Activity Log）**：紀錄使用者登入、登出、頁面開啟、資料異動等事件。

---

## 6. 系統架構

- **後端**：Spring Boot 3.5.6、Spring MVC、Spring Security、Spring Data JPA、AOP。  
- **前端**：Thymeleaf + 原生 CSS/JS，使用共用版面 `layout.css` 與自訂登入樣式 `login.css`。  
- **資料庫**：PostgreSQL（JDBC URL 於 `application.yaml` 設定）。  
- **安全性元件**：
  - `CaptchaValidationFilter`（自訂 `OncePerRequestFilter`），在 `UsernamePasswordAuthenticationFilter` 前驗證使用者輸入的驗證碼並清理 Session 中的驗證碼值。  
  - `CustomAuthenticationSuccessHandler`、`CustomAuthenticationFailureHandler` 與 `CustomLogoutSuccessHandler` 分別處理登入成功、失敗與登出事件，同時串接活動日誌服務。  
  - `ActivityLoggingAspect` 透過 AOP 記錄頁面存取與資料操作，補齊稽核足跡。

部署拓撲：使用者 → Browser/Proxy → Spring Boot 應用 → PostgreSQL 資料庫。

---

## 7. 功能需求

### 7.1 使用者認證
1. 使用者必須通過表單登入；登入頁顯示帳號、密碼、驗證碼欄位（採全新 UI，CSS 路徑 `/css/login.css`）。  
2. 驗證碼由 `/captcha.jpg` 端點產生，`CaptchaValidationFilter` 會於登入提交時從 Session 取得並比對；驗證後即移除 Session 中的值以避免重複使用。  
3. 驗證碼輸入錯誤時立即返回登入頁並顯示專屬錯誤訊息（不計入帳號鎖定次數）。  
4. 帳號或密碼錯誤時顯示一般錯誤訊息；同一帳號於 15 分鐘內連續 5 次錯誤登入，顯示帳號暫時鎖定訊息。  
5. 成功登入後導向 `/`，並將使用者可用功能/群組放入 Session（用於側邊欄渲染）。  
6. 登出後導回登入頁並顯示「已登出」訊息。  
7. 所有登入、失敗、登出事件皆透過 `ActivityLogService` 寫入 `activity_logs`。

### 7.2 角色查詢 (`/role-query`)
- 支援依關鍵字（角色或功能名稱）模糊查詢。  
- 分頁顯示，每頁筆數由 `custom.pagination.page-size` 定義（預設 5）。  
- 顯示角色代碼、名稱、對應功能清單。  
- 若使用者擁有 `/role-edit` 功能，角色代碼顯示為可點選連結。

### 7.3 角色編輯 (`/role-edit`)
- 透過 `roleId` 參數載入既有角色資料與其擁有功能。  
- 提供全選/取消按鈕與個別功能勾選。  
- 儲存時必須檢核角色代碼、名稱不可空白。  
- 儲存成功後顯示成功訊息並保留於頁面；失敗時顯示錯誤原因。

### 7.4 功能維護 (`/function-edtior`)
- 查詢條件：功能 ID、關鍵字；若兩者皆空則列出所有功能。  
- 顯示查詢結果、可修改功能的群組、英文名稱、中文名稱、路徑。  
- 提供新增功能頁 `/function-edtior/new`，含下拉群組選擇。  
- 更新/新增時需檢核欄位不可空白；功能英文名稱唯一。

### 7.5 活動稽核 (`/activity-log-query`)
- 可依使用者、動作類型、日期區間查詢活動紀錄。  
- 支援分頁導航與常用動作列表（登入成功/失敗、功能存取、登出、資料異動）。  
- `ActivityLoggingAspect` 自動記錄控制器存取與資料操作。  
- 統計頁 `/activity-log-query/stats` 提供系統活動概覽（視覺化由前端擴充）。

### 7.6 歡迎頁 (`/`)
- 顯示登入使用者資訊與可訪問功能入口。  
- 側邊欄依 Session 中 `functions` / `groups` 動態渲染。

---

## 8. 非功能需求

| 項目 | 規格 |
| --- | --- |
| 安全性 | 必須使用 HTTPS；密碼以 BCrypt 儲存；啟用驗證碼與登入鎖定。 |
| 可用性 | 介面支援桌面與行動裝置（登入頁已自適應 480px）。 |
| 可維護性 | 以 Service/Repository 分層，AOP 負責錄製活動，易於擴充。 |
| 稽核 | 所有登入、登出、頁面存取、資料異動需寫入 `activity_logs`。 |
| 可靠性 | 操作失敗/成功需具備回饋訊息與例外處理。 |
| 性能 | 頁面查詢預設分頁 5 筆，避免一次載入大量資料；可於 DB 端加索引。 |

---

## 9. 資料模型

| 資料表 | 主要欄位 | 關聯 |
| --- | --- | --- |
| `users` | `id`, `username`, `password` | 與 `user_roles` 關聯角色 |
| `roles` | `id`, `name` | 與 `role_functions` 多對多 `functions` |
| `functions` | `id`, `code`, `name`, `group_id`, `url` | 多對一 `function_group` |
| `function_group` | `id`, `name` | 對應多個 `functions` |
| `role_functions` | `role_id`, `function_id` | 關聯表 |
| `user_roles` | `user_id`, `role_id` | 使用者與角色關聯 |
| `activity_logs` | `id`, `username`, `action_type`, `request_url`, `ip_address`, `created_at` | 記錄事件 |

ER 描述：User 多對多 Role，Role 多對多 Function，Function 屬於 FunctionGroup。

---

## 10. API / 路由一覽

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| GET | `/login` | 顯示登入頁 | 公開 |
| POST | `/login` | 提交登入（含驗證碼） | 公開 |
| GET | `/captcha.jpg` | 取得驗證碼圖片 | 公開 |
| GET | `/logout` | 登出（Spring Security 處理） | 已登入 |
| GET | `/` | 歡迎頁，載入側邊功能 | 已登入 |
| GET | `/role-query` | 角色查詢 | 需授權 |
| GET | `/role-edit` | 角色編輯畫面 | 需授權 |
| POST | `/role-edit` | 儲存角色與功能 | 需授權 |
| GET | `/function-edtior` | 功能維護列表 | 需授權 |
| POST | `/function-edtior` | 更新功能 | 需授權 |
| GET | `/function-edtior/new` | 新增功能表單 | 需授權 |
| POST | `/function-edtior/new` | 建立新功能 | 需授權 |
| GET | `/activity-log-query` | 活動日誌查詢 | 需授權 |
| GET | `/activity-log-query/export` | 依條件匯出（目前重導查詢頁） | 需授權 |
| GET | `/activity-log-query/stats` | 活動統計畫面 | 需授權 |

---

## 11. UI 規格

- **登入頁**：使用 `auth-wrapper` 卡片式設計，背景漸層，含 Font Awesome 圖示。  
- **後台頁面**：採共用 `sidebar-layout`，左側為功能群組，右側為主要內容。  
- 表單欄位具焦點樣式與按鈕互動效果。  
- 提示訊息統一使用 `alert` 與 `auth-alert` 類型。

---

## 12. 例外處理與訊息

| 狀況 | 顯示訊息 | 觸發來源 |
| --- | --- | --- |
| 登入驗證碼錯誤 | 驗證碼錯誤，請重新輸入。 | `CustomAuthenticationFailureHandler` |
| 帳號鎖定 | 帳號暫時鎖定，請稍後再試。 | `ActivityLogService.isLoginFailureExceeded` |
| 一般登入失敗 | 帳號或密碼錯誤。 | Spring Security |
| 角色儲存錯誤 | 儲存失敗：{Exception Message} | `RoleEditorController` |
| 功能更新錯誤 | `IllegalArgumentException` 訊息 | `FunctionService` |
| 功能新增錯誤 | 同上 | `FunctionService.createFunction` |

---

## 13. 安全性需求

1. 所有登入請求需攜帶正確驗證碼；驗證失敗即終止流程。  
2. 密碼加密採用 BCrypt。  
3. 活動日誌需記錄 IP、User-Agent。  
4. CSRF 預設開啟（Spring Security 3.5 預設）。  
5. 靜態資源與驗證碼端點開放存取，其餘頁面須經認證。  
6. 建議部署時配置 HTTPS 與反向代理（Nginx / Apache）。

---

## 14. 系統設定

- 主要設定檔 `application.yaml`  
  - DB 連線資訊（請於部署環境依實際密碼更新）。  
  - `custom.pagination.page-size` 控制分頁筆數。
- Kaptcha 參數於 `CaptchaConfig` 設定（字體大小、長度等）。

---

## 15. 測試計畫（建議）

| 類型 | 測試項目 |
| --- | --- |
| 單元測試 | Service 層 CRUD、驗證邏輯（建議使用 JUnit 5 + Mockito）。 |
| 整合測試 | Spring Security 登入流程、Captcha validation、Repository SQL 結果。 |
| 介面測試 | Login UI、Role/Function 編輯表單、分頁、訊息回饋。 |
| 手動測試 | 連續錯誤登入 -> 鎖定、正確登入 -> 功能載入、角色功能授權 -> 角色查詢顯示。 |

---

## 16. 部署建議

1. 建置環境：JDK 21、Maven 3.9+、PostgreSQL 13+。  
2. 調整 `application.yaml` 連線資訊、JPA `ddl-auto` 與既有 schema 整合。  
3. 使用 `./mvnw clean package` 產出可執行 Jar。  
4. 透過 `java -jar` 或容器化部署，建議搭配反向代理與 TLS。  
5. 排程備份 `activity_logs` 與 RBAC 相關資料表。

---

## 17. 待辦與風險

- **國際化**：目前介面中文為主，未實作多語系。  
- **使用者/角色維護 UI**：`user-edit.html` 等為佔位頁，待後續補全。  
- **Captcha 可用性**：建議加上語音或其他無障礙選項。  
- **稽核匯出**：`/activity-log-query/export` 尚未產出實體檔案。  
- **測試覆蓋率**：目前測試套件不足，需規劃單元與整合測試。

---

## 18. 參考資料

- Spring Boot 3.5 官方文件  
- Spring Security Reference 6.x  
- Google Kaptcha GitHub（Apache License 2.0）

---

> 本規格書以現行程式碼為基礎整理，後續若新增功能或調整資料結構，請同步更新本文件。
