# [1.1.0](https://github.com/omarr78/Internship-HR-Management-API/compare/v1.0.0...v1.1.0) (2026-02-01)


### Bug Fixes

* fix the validation logic for start date must be in the current month or after ([1fa652c](https://github.com/omarr78/Internship-HR-Management-API/commit/1fa652c54103d4c89421d0dd52165511bdfa0d30))


### Features

* add validation as an aspect before created ValidateLeavesDates Annotation ([10308de](https://github.com/omarr78/Internship-HR-Management-API/commit/10308deacbffeec5128d267f466024774caec590))
* **db:** add leave table in migration and the entity ([58e044f](https://github.com/omarr78/Internship-HR-Management-API/commit/58e044f7109b1b75b00b19fbac3e5480863c0e70))
* **db:** add leave table in migration and the entity1 ([fd66a60](https://github.com/omarr78/Internship-HR-Management-API/commit/fd66a6075fe2c3de8777c1269a686fff24b3f4ec))
* handling duplicate exception ([c5810bb](https://github.com/omarr78/Internship-HR-Management-API/commit/c5810bbdc1843438f88d52a456223f4241d040fe))
* implement exceeded leave days limit for long standing & recently joined employees ([ffefd98](https://github.com/omarr78/Internship-HR-Management-API/commit/ffefd98917a9eb861598f2a7bc7ecbc47ba38490))
* **leave-controller:** add implementation in controller to pass test ([3efff80](https://github.com/omarr78/Internship-HR-Management-API/commit/3efff804d3797d3c9c1cf0be57e4871d47636306))
* **leave-controller:** implement adding leaves in weekends scenario ([50b8227](https://github.com/omarr78/Internship-HR-Management-API/commit/50b82270e02f85e7cd33c3ab20d4f0277bd9bf15))

# [1.0.0](https://github.com/omarr78/Internship-HR-Management-API/compare/v0.1.1...v1.0.0) (2026-01-14)


* feat(employee)!: update employee domain, validation, and persistence model ([24d4e09](https://github.com/omarr78/Internship-HR-Management-API/commit/24d4e096fb0d39be2a1059860d317ba79bb7219b))


### Bug Fixes

* **employee:** make the degree column varchar instead of enum to be compatable with h2 database ([9fc9e05](https://github.com/omarr78/Internship-HR-Management-API/commit/9fc9e05b1d4f026bae457979ef19f12cf29ef90b))
* **migration-file:** fix the default date ([bcecd32](https://github.com/omarr78/Internship-HR-Management-API/commit/bcecd3211c0dd028c885059031fe62ec10465517))


### Features

* **employee:** add update and add new fields in entity and create a new migration file & refactor mapper and service ([1a453bc](https://github.com/omarr78/Internship-HR-Management-API/commit/1a453bc509f471d601ff5a2579eb682c3dae85ff))
* **validation:** add annotation to mark methods for validation interception ([48a4a1a](https://github.com/omarr78/Internship-HR-Management-API/commit/48a4a1a959f775fa7902698565210727e89608c2))
* **validation:** add method validation aspect using AOP ([2976082](https://github.com/omarr78/Internship-HR-Management-API/commit/2976082436f88dd9cabb208bec12252d0bf34e91))


### BREAKING CHANGES

* employee DTOs, entity fields, validation rules, and database schema were modified

## [0.1.1](https://github.com/omarr78/Internship-HR-Management-API/compare/v0.1.0...v0.1.1) (2025-12-31)


### Bug Fixes

* **review:** change the logic of asserting two lists ([d9f63c0](https://github.com/omarr78/Internship-HR-Management-API/commit/d9f63c0d0cb3ce386c8e42bc3c092092bad4f2be))
* **review:** fix review resource path should be single noun not plural ([dce0076](https://github.com/omarr78/Internship-HR-Management-API/commit/dce0076a82620eb49d2a6e4f0c68b93a529fa625))
* **review:** use 1,2,3 for team naming or A,B,C not using both ([9fe9d55](https://github.com/omarr78/Internship-HR-Management-API/commit/9fe9d554df364f45400f0ac551afde3d6a33eeaf))
* **team-controller-test:** refactor resource path in tests ([ea80002](https://github.com/omarr78/Internship-HR-Management-API/commit/ea80002b5a822040f3c8a11ad8a285c8d43cbc2c))

# [0.1.0](https://github.com/omarr78/Internship-HR-Management-API/compare/v0.0.0...v0.1.0) (2025-12-20)


### Features

* **ci:** add new feature to test semantic release ([7d1720b](https://github.com/omarr78/Internship-HR-Management-API/commit/7d1720b3ba565d8d507dff71c3b33dbdecd636a7))
