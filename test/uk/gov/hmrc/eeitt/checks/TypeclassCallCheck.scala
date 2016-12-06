package uk.gov.hmrc.eeitt.checks

trait TypeclassCallCheck { def call(): Unit }
trait AuditCheck extends TypeclassCallCheck
trait FindUserCheck extends TypeclassCallCheck
trait AddRegistrationCheck extends TypeclassCallCheck
trait FindRegistrationCheck extends TypeclassCallCheck
