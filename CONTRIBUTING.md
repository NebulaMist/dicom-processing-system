# 贡献指南

感谢您对DICOM医学影像数据处理系统的关注！我们欢迎任何形式的贡献。

## 如何贡献

### 报告问题
- 使用GitHub Issues报告bug或建议新功能
- 提供详细的问题描述和重现步骤
- 包含相关的错误信息和日志

### 代码贡献

#### 开发环境设置
1. Fork这个仓库
2. 克隆你的fork到本地
```bash
git clone https://github.com/YOUR_USERNAME/dicom-processing-system.git
cd dicom-processing-system
```

3. 安装依赖
```bash
mvn clean install
```

4. 运行测试确保一切正常
```bash
mvn test
```

#### 代码标准
- 使用Java 11+
- 遵循Google Java代码风格
- 为新功能编写单元测试
- 保持测试覆盖率在80%以上
- 添加适当的JavaDoc注释

#### 提交流程
1. 创建feature分支
```bash
git checkout -b feature/your-feature-name
```

2. 进行开发
- 遵循TDD原则，先写测试再写实现
- 确保所有测试通过
- 更新相关文档

3. 提交变更
```bash
git add .
git commit -m "feat: add your feature description"
```

4. 推送到你的fork
```bash
git push origin feature/your-feature-name
```

5. 创建Pull Request
- 提供清晰的PR描述
- 链接相关的Issues
- 确保CI检查通过

### 代码审查
- 所有PR都需要至少一个维护者的审查
- 解决所有审查意见后方可合并
- 保持分支历史清晰

## 开发指南

### 项目结构
```
src/main/java/com/dicom/
├── dictionary/     # DICOM数据字典
├── data/          # 数据结构和文件处理
├── vr/            # VR类型处理
├── transfersyntax/ # 传输语法
├── gui/           # 图形界面
└── application/   # 应用程序
```

### 重要的设计模式
- **组合模式**: 用于DICOM数据结构
- **享元模式**: 用于VR类型管理
- **工厂模式**: 用于VR实例创建

### 测试策略
- 单元测试：测试单个类或方法
- 集成测试：测试组件间的交互
- 功能测试：测试完整的DICOM文件处理流程

### 新功能开发
1. 理解DICOM标准相关部分
2. 设计API接口
3. 编写测试用例
4. 实现功能
5. 更新文档

## 版本发布

### 版本号规则
遵循语义化版本控制 (SemVer)：
- MAJOR.MINOR.PATCH
- MAJOR: 不兼容的API修改
- MINOR: 向后兼容的功能性新增
- PATCH: 向后兼容的问题修正

### 发布检查清单
- [ ] 所有测试通过
- [ ] 代码覆盖率满足要求
- [ ] 文档已更新
- [ ] CHANGELOG已更新
- [ ] 版本号已更新

## 社区

### 行为准则
- 尊重所有贡献者
- 使用建设性的语言
- 专注于对社区最有利的事情
- 表现出同理心

### 支持
- 查看[Issues](https://github.com/YOUR_USERNAME/dicom-processing-system/issues)
- 阅读[Wiki](https://github.com/YOUR_USERNAME/dicom-processing-system/wiki)
- 参与[Discussions](https://github.com/YOUR_USERNAME/dicom-processing-system/discussions)

## 许可证
通过贡献代码，您同意您的贡献将在与项目相同的许可证下授权。

感谢您的贡献！
