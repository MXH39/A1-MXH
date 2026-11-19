# A1-username — 诊所预约系统（Java 8）

面向对象设计的“骨架”实现：没有界面、没有菜单、没有登录、没有持久化，只有一组职责清晰的类，
以及 `AssignmentOne.main` 里一段脚本化演示（创建对象 → 调用行为 → 打印结果）。

> 提交前请把项目文件夹 `A1-username` 改成 `A1-你的MySCU用户名`（例如 `A1-lfolkman`）。

---

## 1. 编译与运行

Java 8 已自带 `java.time`，无需任何第三方依赖。

```bash
# 在项目根目录 A1-username/ 下
javac -encoding UTF-8 -d out src/clinic/*.java src/clinic/staff/*.java src/clinic/booking/*.java
java  -Dfile.encoding=UTF-8 -cp out clinic.AssignmentOne
```

Windows 下可直接双击 / 执行 `run.bat`；Git Bash、macOS、Linux 用 `./run.sh`。

如果命令行提示 `javac: command not found`，说明 JDK 的 `bin` 目录不在 `PATH` 里（本机就是这种情况），
用完整路径即可：

```bash
"/c/Program Files/Java/jdk1.8.0_271/bin/javac" -encoding UTF-8 -d out src/clinic/*.java src/clinic/staff/*.java src/clinic/booking/*.java
"/c/Program Files/Java/jdk1.8.0_271/bin/java"  -Dfile.encoding=UTF-8 -cp out clinic.AssignmentOne
```

IDEA / Eclipse：直接 `Open` 项目根目录，把 `src` 标为 Sources Root，运行 `clinic.AssignmentOne`。

生成 API 文档（验证 Javadoc 完整性）：

```bash
javadoc -encoding UTF-8 -charset UTF-8 -d doc src/clinic/*.java
```

---

## 2. 类设计总览

**包结构**（按职责分组，入口不变，仍是 `clinic.AssignmentOne`）：

```
src/clinic/           入口与公共工具
  ├─ AssignmentOne    脚本化演示（main）
  └─ Validation       统一的参数校验工具（public，供两个子包共用）
src/clinic/staff/     医护人员（第 1–3 部分）
  ├─ HealthProfessional (abstract)   通用医护基类
  ├─ Prescriber                      开处方能力接口
  └─ GeneralPractitioner / Specialist / Surgeon / Dietitian / Nurse
src/clinic/booking/   预约（第 4–6 部分）
  ├─ Appointment          单个预约
  ├─ AppointmentManager   预约集合管理
  └─ ClinicHours          门诊时段表（08:00–16:00 / 30 分钟）
```

```
                        +------------------------------+
                        |  HealthProfessional (abstract)|
                        |  - id : String               |
                        |  - name : String             |
                        |  - workingDays : Set<DayOfWeek> (unmodifiable)
                        |  + getRole()          abstract
                        |  # describeSpecialisation() abstract
                        |  + toString() / equals() / hashCode()
                        +--------------+---------------+
                                       ^ 继承（is-a）
        +--------------+---------------+--------------+---------------+
        |              |               |              |               |
GeneralPractitioner  Specialist     Surgeon       Dietitian        Nurse
        |              |               |              |               |
        +--------------+---------------+              |               |
                  implements                          |               |
                        |                             |               |
                  +-----+------+                      |               |
                  | Prescriber |  （能力接口：只有医生能开处方）      |
                  +------------+                      |               |

   Appointment  ──1──▶ HealthProfessional   （任意具体类型都可）
        - patientName, patientPhone, LocalDate date, LocalTime time
        - implements Comparable<Appointment>  → 按 日期→时间 排序

   AppointmentManager ──▶ ClinicHours
        - Map<String, Appointment>  key = "professionalId@date@HH:mm"

   ClinicHours  - opening 08:00, closing 16:00, slot 30 min → 自动生成 16 个时段
   Validation   - clinic 包公共工具类，统一 id / 文本 / 手机号 / 非空 / 非负 校验
```

---

## 3. 各部分的实现与设计论证

### 第 1 部分：通用医护人员 `HealthProfessional`

| 设计决策 | 理由 |
| --- | --- |
| 字段 `id`、`name`、`workingDays` 全部 `private final` | 这些是人的事实，建模期间不该改变；`final` 让对象天生不可变，外部无法把对象改成非法状态 |
| 构造器内校验（id 只能是数字、姓名不能空、排班不能为空） | “非法状态不存在”优于“事后检查”：对象一旦创建成功就一定合法 |
| `workingDays` 用 `EnumSet`，并用 `Collections.unmodifiableSet` 包装 | `EnumSet` 只接受 `DayOfWeek` 常量、天然按日历顺序迭代（打印排班不用排序）、极省内存；不可修改视图把 `get` 变成只读访问 |
| 传入的集合做**防御性拷贝** | 调用方保留原集合的引用也无法修改我们的内部状态 |
| `equals/hashCode` 基于 `id` | 两个对象代表同一个人当且仅当 id 相同——第 5 部分判断“两个预约是否属于同一医护”就靠它 |
| `toString()` 输出角色、id、姓名、排班、专有信息 | `System.out.println(doctorA)` 直接可读 |
| 两个构造器：`(id, name, Set<DayOfWeek>)` 与 `(id, name)` | 多数全职员工都是周一到周五，调用方不必凭空编造排班；需要特殊排班时仍完全可控 |
| `protected` 构造器 + `abstract` 类 | 外部不能 `new HealthProfessional(...)`，只能通过具体角色创建 |

### 第 2 部分：四个以上具体类型

- **继承复用公共代码**：`GeneralPractitioner`、`Specialist`、`Surgeon`、`Dietitian`、`Nurse` 都继承
  `HealthProfessional`，共有信息只在父类写一次；每个子类只声明自己独有的字段。
- **用抽象类阻止“通用医护”被实例化**：题目提示的正是这一点——`abstract` 让“创建一个没有专业的医护”
  变成编译错误，而不是运行时的一条怪数据。
- **`Prescriber` 接口而不是父类方法**：开处方是“能力”而非“身份”。营养师/护士不是 `Prescriber`，
  编译器就能保证不会对它们调用 `prescribe`；将来若加入药师，只需实现接口，不必改父类。
- **打印时同时包含共有信息和专有信息**：父类的 `toString()` 是**模板方法**——它拼好公共部分并调用
  `describeSpecialisation()`，由子类填空。子类不会“忘记”打印公共信息。
- **清晰标明类型**：`getRole()` 返回 `"Specialist (Paediatrics)"`、`"Surgeon (Orthopaedic surgery)"`，
  放进任何列表都能一眼看出是什么角色。
- **同一父类型统一处理**：`List<HealthProfessional>` 里混放 9 个人，打印、遍历、传参都只有一种写法；
  运行期各对象按自己的实现响应（多态）。

### 第 3 部分：操作对象

`main` 创建 3 名全科医生 + 2 名专科医生 + 1 名外科医生 + 1 名营养师 + 2 名护士，
放进**同一个** `List<HealthProfessional>` 遍历打印。
新增一种医护类型时，这段代码**一行都不用改**——它只依赖父类型。

### 第 4 部分：`Appointment`

- **患者不单独建类**：本系统中患者没有身份、没有被查询、没有历史，只是预约上的两个字段
  （姓名、手机号）。造一个 `Patient` 类只会增加一层无行为的间接。若将来需要患者档案，抽取成本也很低。
- **预约必须始终完整**：构造器拒绝空白姓名、非法手机号、空医护、空日期/时间，
  因此“存在的 `Appointment` 一定合法”。时段对齐、营业时间、重复预约这些**依赖门诊日程和其他预约**
  的规则放在 `AppointmentManager`，因为单个预约并不知道当天还有什么安排。
- **字段用 `LocalDate` / `LocalTime` 而不是 `String`**：时间按字符串排序会把 `"10:00"` 排在 `"9:00"` 前；
  `java.time` 的比较是真正的时序比较。
- **`implements Comparable<Appointment>`**：预约自己知道“我和别人谁先谁后”（日期 → 时间 → 医护 id 兜底稳定排序），
  任何集合都能 `Collections.sort(...)`，不需要额外的 `Comparator`。
- **`toString()` 两行输出**：第一行患者与时间，第二行缩进显示医护的全部详情（调用医护自己的 `toString()`）。

### 第 5 部分：`AppointmentManager` + `ClinicHours`

- **用 `Map<String, Appointment>` 而不是 `List`**：诊所最常问的问题是“这位医护这个时段有空吗”，
  最重要的规则是“同一医护同一时段不能约两次”。以 `professionalId@date@HH:mm` 为键，
  判断冲突是一次 O(1) 查表，而且**重复预约在数据结构层面就不可能存在**。
  `LinkedHashMap` 保留插入顺序，便于对照调试（正式展示仍然排序）。
- **固定时段由 `ClinicHours` 生成**：08:00 / 16:00 / 30 分钟三个值生成 16 个时段，其余类**不硬编码任何时间**。
  改成 09:00–17:00 或 20 分钟一段，只改一处构造参数，校验、空闲时段查询全部自动跟随。
- **任意类型医护都能被预约**：`book(...)` 的参数类型是 `HealthProfessional`，不关心具体角色。
- **“两医护是否相同”**：`HealthProfessional.equals/hashCode` 基于 id；`AppointmentManager` 的键也用 id，
  两者语义一致。
- **操作结果返回 `String` 消息**：成功、拒绝都有明确原因（`BOOKED` / `REJECTED` / `CANCELLED`）。
  返回 `boolean` 会把“为什么失败”丢掉，调用方必然要靠猜。
- **取消**：按“医护 id + 时段”（`cancel`）或按“患者手机号”批量取消（`cancelAllForPatient`）；
  取消不存在的预约返回警告而不是抛异常或静默失败。
- **额外能力 `getAvailableSlots(...)`**：展示“时段由 `ClinicHours` 生成”这一设计的收益。

### 第 6 部分：演示

`AssignmentOne.main` 依次演示：

1. 打印全部员工（含排班）→ 证明多态打印；
2. 同一句 `prescribe` / `takeBloodSample` / `reviewDiet` 在不同角色上得到不同结果；
3. 跨 5 种医护类型添加 7 个预约（含同一时段不同医护的合法并行预约）；
4. 全部预约按时间排序展示；按医护 id 筛选；按手机号（带空格输入）筛选；查看某医护剩余时段；
5. **失败场景**：重复预约、医护当天不上班、手机号非法、09:15/07:30 非门诊时段、预约明天；
6. **取消场景**：正常取消、取消不存在的预约、按手机号批量取消、取消一个不存在的号码；
7. 再次打印全部预约，证明集合确实已更新（7 → 5）。

---

## 4. 明确的假设与取舍

1. **只能预约“今天”**：`AppointmentManager` 拒绝任何非今天的日期。当天已过去的时间点不做拦截
   （题目把复杂度限定在“仅限当天”，未要求时钟校验）。
2. **值班日校验**：除了题目要求的三条规则（08:00–16:00、30 分钟时段、不可重复预约），
   还额外校验“该医护当天上班”。这让排班字段真正参与业务，也让失败演示更有意义。
3. **演示数据把“今天”并入排班**（`roster(...)` 辅助方法）：排班本身是每周固定的，
   但如果硬编码成“周一/三/五”，在周六运行脚本时全员都不上班，演示就只剩下失败案例。
   代码里对此有注释说明。
4. **手机号**：去掉空格与连字符后必须是 8–15 位数字；存储为纯数字，因此 `0433 111 222`
   和 `0433111222` 能查到同一批预约。
5. **同一患者可以在同一时段约不同医护**（例如带两个孩子看不同的医生），题目允许。
6. **改约 = 取消 + 重新预约**：`Appointment` 不可变，符合“预约是一次具体承诺”的语义。
7. **未实现（有意为之）**：界面、菜单、登录、数据库、并发控制、按医护/按天分表。

---

## 5. 扩展点（说明设计是否真的“开闭”）

| 需求变化 | 需要改动的代码 |
| --- | --- |
| 新增一种医护（如 Physiotherapist） | 只新增一个 `extends HealthProfessional` 的类；`main` 里加一行创建。其余代码零改动 |
| 门诊改成 09:00–17:00、20 分钟一段 | `new ClinicHours(LocalTime.of(9,0), LocalTime.of(17,0), 20)` 传给 `AppointmentManager`，别的类不改 |
| 允许预约未来日期 | 删掉 `checkBookingRules` 中“仅今天”那一条；键里已经含日期，无需其它改动 |
| 患者需要档案/病史 | 提取 `Patient` 类，作为 `Appointment` 的一个字段（局部改动） |
| 需要“某医护某天全部时段” | 已有 `getAvailableSlots(...)`，把日期参数化即可 |
