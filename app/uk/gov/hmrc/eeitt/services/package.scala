package uk.gov.hmrc.eeitt

import uk.gov.hmrc.eeitt.model.{ RegisterBusinessUserRequest, RegisterAgentRequest }
import uk.gov.hmrc.eeitt.repositories._

package object implicits {

  implicit lazy val registrationRepo = registrationRepository
  implicit lazy val agentRegistrationRepo = agentRegistrationRepository
  implicit lazy val etmpBusinessUserRepo = etmpBusinessUserRepository
  implicit lazy val etmpAgentRepo = etmpAgentRepository

  implicit def addReg(req: RegisterAgentRequest)(implicit repository: RegistrationAgentRepository) = {
    repository.register(req)
  }

  implicit def addReg(req: RegisterBusinessUserRequest)(implicit repository: RegistrationRepository) = {
    repository.register(req)
  }
}
