# Alpay Remittance Platform: Class Diagram Specification

Complete list of classes, attributes, methods and relationships in the class diagram. Getters and setters are omitted on every class.

**Notation:** `-` private, `+` public, *(static)* is drawn underlined, *(abstract)* is drawn in italics. Multiplicities: `1` exactly one, `0..1` none or one, `0..*` zero or more, `1..*` at least one.

## Ecosystem structure

### EcoSystem

**Attributes**

- `- instance : EcoSystem` *(static)*
- `- networks : List<Network>`
- `- systemAdmins : List<UserAccount>`

**Methods**

- `+ getInstance() : EcoSystem` *(static)*
- `+ createNetwork(name : String) : Network`
- `+ findNetwork(name : String) : Network`
- `+ authenticateUser(username : String, password : String) : UserAccount`

### Network

**Attributes**

- `- networkId : int`
- `- name : String`
- `- enterprises : List<Enterprise>`

**Methods**

- `+ createEnterprise(name : String, type : String) : Enterprise`
- `+ deleteEnterprise(enterprise : Enterprise) : void`

### «abstract» Enterprise

**Attributes**

- `- enterpriseId : int`
- `- name : String`
- `- organizations : List<Organization>`
- `- enterpriseAdmins : List<UserAccount>`

**Methods**

- `+ createOrganization(type : String) : Organization`
- `+ deleteOrganization(org : Organization) : void`
- `+ getSupportedOrganizations() : List<String>` *(abstract)*

### MTOEnterprise (extends Enterprise)

*Belongs to: Canada*

**Attributes**

- `- customers : List<UserAccount>`

**Methods**

- `+ registerCustomer(account : UserAccount) : void`
- `+ getSupportedOrganizations() : List<String>`

### AlpayEnterprise (extends Enterprise)

*Belongs to: Ghana*

**Methods**

- `+ getSupportedOrganizations() : List<String>`

### PartnerBankEnterprise (extends Enterprise)

*Belongs to: Ghana*

**Methods**

- `+ getSupportedOrganizations() : List<String>`

### «abstract» Organization

**Attributes**

- `- organizationId : int`
- `- name : String`
- `- workQueue : WorkQueue`
- `- userAccounts : List<UserAccount>`

**Methods**

- `+ createUserAccount(username : String, password : String, person : Person, role : Role) : UserAccount`
- `+ deleteUserAccount(account : UserAccount) : void`
- `+ getSupportedRoles() : List<Role>` *(abstract)*

### CustomerOperationsOrganization (extends Organization)

*Belongs to: MTO*

**Methods**

- `+ getSupportedRoles() : List<Role>`

### ComplianceRiskOrganization (extends Organization)

*Belongs to: Alpay*

**Methods**

- `+ getSupportedRoles() : List<Role>`

### CustomerServiceOrganization (extends Organization)

*Belongs to: Alpay*

**Methods**

- `+ getSupportedRoles() : List<Role>`

### SettlementPayoutsOrganization (extends Organization)

*Belongs to: Partner Bank*

**Methods**

- `+ getSupportedRoles() : List<Role>`

## Users and roles

### UserAccount

**Attributes**

- `- accountId : int`
- `- username : String`
- `- password : String`
- `- role : Role`
- `- person : Person`
- `- beneficiaries : List<Beneficiary>`

**Methods**

- `+ addBeneficiary(beneficiary : Beneficiary) : void`
- `+ removeBeneficiary(beneficiary : Beneficiary) : void`

### Person

**Attributes**

- `- personId : int`
- `- fullName : String`
- `- email : String`
- `- phone : String`
- `- country : String`

### «abstract» Role

**Methods**

- `+ createWorkArea(account : UserAccount, org : Organization, enterprise : Enterprise, system : EcoSystem) : JPanel` *(abstract)*
- `+ toString() : String`

### SenderRole (extends Role)

*Belongs to: MTO customer*

**Methods**

- `+ createWorkArea(...) : JPanel`
- `+ toString() : String`

### MTOOfficerRole (extends Role)

*Belongs to: MTO*

**Methods**

- `+ createWorkArea(...) : JPanel`
- `+ toString() : String`

### ComplianceOfficerRole (extends Role)

*Belongs to: Alpay*

**Methods**

- `+ createWorkArea(...) : JPanel`
- `+ toString() : String`

### CustomerAgentRole (extends Role)

*Belongs to: Alpay*

**Methods**

- `+ createWorkArea(...) : JPanel`
- `+ toString() : String`

### BankOfficerRole (extends Role)

*Belongs to: Partner Bank*

**Methods**

- `+ createWorkArea(...) : JPanel`
- `+ toString() : String`

### EnterpriseAdminRole (extends Role)

*Belongs to: one per enterprise*

**Methods**

- `+ createWorkArea(...) : JPanel`
- `+ toString() : String`

### SystemAdminRole (extends Role)

*Belongs to: whole platform*

**Methods**

- `+ createWorkArea(...) : JPanel`
- `+ toString() : String`

## Transfers and work requests

### Beneficiary

**Attributes**

- `- beneficiaryId : int`
- `- fullName : String`
- `- phone : String`
- `- payoutMethod : PayoutMethod`
- `- walletNumber : String`
- `- bankName : String`
- `- accountNumber : String`

### «enumeration» PayoutMethod

**Values**

- `MOMO_WALLET`
- `BANK_ACCOUNT`

### Transfer

**Attributes**

- `- transferId : int`
- `- reference : String`
- `- sender : UserAccount`
- `- beneficiary : Beneficiary`
- `- amountCad : double`
- `- fee : double`
- `- exchangeRate : double`
- `- amountGhs : double`
- `- status : TransferStatus`
- `- createdAt : LocalDateTime`

**Methods**

- `+ generateReference() : String`
- `+ calculateAmountGhs() : double`
- `+ updateStatus(newStatus : TransferStatus) : void`

### «enumeration» TransferStatus

**Values**

- `SUBMITTED`
- `MTO_APPROVED`
- `ALPAY_APPROVED`
- `SETTLED`
- `PAID`
- `RECONCILED`
- `REJECTED`

### WorkQueue

**Attributes**

- `- workRequests : List<WorkRequest>`

**Methods**

- `+ addRequest(request : WorkRequest) : void`
- `+ removeRequest(request : WorkRequest) : void`
- `+ getRequestsByStatus(status : String) : List<WorkRequest>`

### «abstract» WorkRequest

**Attributes**

- `- requestId : int`
- `- sender : UserAccount`
- `- receiver : UserAccount`
- `- transfer : Transfer`
- `- status : String`
- `- message : String`
- `- requestDate : LocalDateTime`
- `- resolveDate : LocalDateTime`

**Methods**

- `+ markResolved(resolvedBy : UserAccount) : void`
- `+ getRequestType() : String` *(abstract)*

### TransferRequest (extends WorkRequest)

*Belongs to: Sender → MTO Officer*

**Attributes**

- `- verificationNotes : String`

**Methods**

- `+ getRequestType() : String`

### PaymentInstruction (extends WorkRequest)

*Belongs to: MTO → Alpay*

**Attributes**

- `- screeningNotes : String`

**Methods**

- `+ getRequestType() : String`

### SettlementRequest (extends WorkRequest)

*Belongs to: Alpay → Partner Bank*

**Attributes**

- `- settledAmountGhs : double`

**Methods**

- `+ getRequestType() : String`

### CustomerQuery (extends WorkRequest)

*Belongs to: Sender → Alpay Customer Agent*

**Attributes**

- `- question : String`
- `- response : String`

**Methods**

- `+ getRequestType() : String`

### ReconciliationReport

*Belongs to: MTO and Alpay*

**Attributes**

- `- transfers : List<Transfer>`

**Methods**

- `+ generate(from : LocalDate, to : LocalDate) : void`
- `+ findMismatches() : List<Transfer>`
- `+ findStuckTransfers(days : int) : List<Transfer>`
- `+ markReconciled(transfer : Transfer) : void`

## Database layer

### «interface» CrudDAO<T>

**Methods**

- `+ create(item : T) : void`
- `+ read(id : int) : T`
- `+ readAll() : List<T>`
- `+ update(item : T) : void`
- `+ delete(id : int) : void`

### UserAccountDAO (implements CrudDAO<T>)

**Methods**

- `+ create(item : UserAccount) : void`
- `+ read(id : int) : UserAccount`
- `+ readAll() : List<UserAccount>`
- `+ update(item : UserAccount) : void`
- `+ delete(id : int) : void`

### BeneficiaryDAO (implements CrudDAO<T>)

**Methods**

- `+ create(item : Beneficiary) : void`
- `+ read(id : int) : Beneficiary`
- `+ readAll() : List<Beneficiary>`
- `+ update(item : Beneficiary) : void`
- `+ delete(id : int) : void`

### TransferDAO (implements CrudDAO<T>)

**Methods**

- `+ create(item : Transfer) : void`
- `+ read(id : int) : Transfer`
- `+ readAll() : List<Transfer>`
- `+ update(item : Transfer) : void`
- `+ delete(id : int) : void`

### WorkRequestDAO (implements CrudDAO<T>)

**Methods**

- `+ create(item : WorkRequest) : void`
- `+ read(id : int) : WorkRequest`
- `+ readAll() : List<WorkRequest>`
- `+ update(item : WorkRequest) : void`
- `+ delete(id : int) : void`

### EnterpriseDAO (implements CrudDAO<T>)

**Methods**

- `+ create(item : Enterprise) : void`
- `+ read(id : int) : Enterprise`
- `+ readAll() : List<Enterprise>`
- `+ update(item : Enterprise) : void`
- `+ delete(id : int) : void`

### OrganizationDAO (implements CrudDAO<T>)

**Methods**

- `+ create(item : Organization) : void`
- `+ read(id : int) : Organization`
- `+ readAll() : List<Organization>`
- `+ update(item : Organization) : void`
- `+ delete(id : int) : void`

### DBConnection

**Attributes**

- `- instance : DBConnection` *(static)*
- `- connection : Connection`
- `- URL : String` *(static)*
- `- USER : String` *(static)*
- `- PASSWORD : String` *(static)*

**Methods**

- `+ getInstance() : DBConnection` *(static)*
- `+ getConnection() : Connection`
- `+ closeConnection() : void`

## User interface

### MainJFrame (extends JFrame)

**Attributes**

- `- system : EcoSystem`
- `- container : JPanel`

**Methods**

- `+ main(args : String[]) : void` *(static)*
- `- loginButtonActionPerformed(evt : ActionEvent) : void`
- `- logoutButtonActionPerformed(evt : ActionEvent) : void`

### SenderWorkAreaPanel (extends JPanel)

*Belongs to: MTO customer*

No attributes or methods beyond the Swing components it contains.

### MTOOfficerWorkAreaPanel (extends JPanel)

*Belongs to: MTO*

No attributes or methods beyond the Swing components it contains.

### ComplianceOfficerWorkAreaPanel (extends JPanel)

*Belongs to: Alpay*

No attributes or methods beyond the Swing components it contains.

### CustomerAgentWorkAreaPanel (extends JPanel)

*Belongs to: Alpay*

No attributes or methods beyond the Swing components it contains.

### BankOfficerWorkAreaPanel (extends JPanel)

*Belongs to: Partner Bank*

No attributes or methods beyond the Swing components it contains.

### EnterpriseAdminWorkAreaPanel (extends JPanel)

*Belongs to: one per enterprise*

No attributes or methods beyond the Swing components it contains.

### SystemAdminWorkAreaPanel (extends JPanel)

*Belongs to: whole platform*

No attributes or methods beyond the Swing components it contains.

### ReconciliationReportPanel (extends JPanel)

*Belongs to: MTO and Alpay*

No attributes or methods beyond the Swing components it contains.

## Relationships

| From | Relationship | To | Multiplicity | Label |
|---|---|---|---|---|
| EcoSystem | composition | Network | 1 to 1..* | contains |
| EcoSystem | aggregation | UserAccount | 0..1 to 1..* | system admins |
| Network | composition | Enterprise | 1 to 1..* | contains |
| MTOEnterprise | inheritance (extends) | Enterprise | — | — |
| AlpayEnterprise | inheritance (extends) | Enterprise | — | — |
| PartnerBankEnterprise | inheritance (extends) | Enterprise | — | — |
| Enterprise | composition | Organization | 1 to 1..* | contains |
| Enterprise | aggregation | UserAccount | 0..1 to 1..* | enterprise admins |
| MTOEnterprise | aggregation | UserAccount | 0..1 to 0..* | customers |
| CustomerOperationsOrganization | inheritance (extends) | Organization | — | — |
| ComplianceRiskOrganization | inheritance (extends) | Organization | — | — |
| CustomerServiceOrganization | inheritance (extends) | Organization | — | — |
| SettlementPayoutsOrganization | inheritance (extends) | Organization | — | — |
| Organization | composition | WorkQueue | 1 to 1 | has |
| Organization | aggregation | UserAccount | 0..1 to 0..* | employs |
| UserAccount | association | Role | 0..* to 1 | has |
| UserAccount | association | Person | 1 to 1 | belongs to |
| UserAccount | aggregation | Beneficiary | 1 to 0..* | saves |
| SenderRole | inheritance (extends) | Role | — | — |
| MTOOfficerRole | inheritance (extends) | Role | — | — |
| ComplianceOfficerRole | inheritance (extends) | Role | — | — |
| CustomerAgentRole | inheritance (extends) | Role | — | — |
| BankOfficerRole | inheritance (extends) | Role | — | — |
| EnterpriseAdminRole | inheritance (extends) | Role | — | — |
| SystemAdminRole | inheritance (extends) | Role | — | — |
| SenderRole | dependency | SenderWorkAreaPanel | — | creates |
| MTOOfficerRole | dependency | MTOOfficerWorkAreaPanel | — | creates |
| ComplianceOfficerRole | dependency | ComplianceOfficerWorkAreaPanel | — | creates |
| CustomerAgentRole | dependency | CustomerAgentWorkAreaPanel | — | creates |
| BankOfficerRole | dependency | BankOfficerWorkAreaPanel | — | creates |
| EnterpriseAdminRole | dependency | EnterpriseAdminWorkAreaPanel | — | creates |
| SystemAdminRole | dependency | SystemAdminWorkAreaPanel | — | creates |
| MTOOfficerWorkAreaPanel | dependency | ReconciliationReportPanel | — | opens |
| ComplianceOfficerWorkAreaPanel | dependency | ReconciliationReportPanel | — | opens |
| ReconciliationReportPanel | association | ReconciliationReport | 1 to 1 | displays |
| ReconciliationReport | aggregation | Transfer | 0..* to 0..* | checks |
| Beneficiary | association | PayoutMethod | 0..* to 1 | uses |
| Transfer | association | UserAccount | 0..* to 1 | sent by |
| Transfer | association | Beneficiary | 0..* to 1 | pays |
| Transfer | association | TransferStatus | 0..* to 1 | has |
| WorkQueue | aggregation | WorkRequest | 1 to 0..* | holds |
| TransferRequest | inheritance (extends) | WorkRequest | — | — |
| PaymentInstruction | inheritance (extends) | WorkRequest | — | — |
| SettlementRequest | inheritance (extends) | WorkRequest | — | — |
| CustomerQuery | inheritance (extends) | WorkRequest | — | — |
| WorkRequest | association | UserAccount | 0..* to 1 | sender |
| WorkRequest | association | UserAccount | 0..* to 0..1 | receiver |
| WorkRequest | association | Transfer | 1..* to 1 | concerns |
| UserAccountDAO | implements | CrudDAO | — | — |
| BeneficiaryDAO | implements | CrudDAO | — | — |
| TransferDAO | implements | CrudDAO | — | — |
| WorkRequestDAO | implements | CrudDAO | — | — |
| EnterpriseDAO | implements | CrudDAO | — | — |
| OrganizationDAO | implements | CrudDAO | — | — |
| UserAccountDAO | dependency | DBConnection | — | uses |
| BeneficiaryDAO | dependency | DBConnection | — | uses |
| TransferDAO | dependency | DBConnection | — | uses |
| WorkRequestDAO | dependency | DBConnection | — | uses |
| EnterpriseDAO | dependency | DBConnection | — | uses |
| OrganizationDAO | dependency | DBConnection | — | uses |
| MainJFrame | association | EcoSystem | 1 to 1 | uses |
| MainJFrame | dependency | Role | — | opens work area |
