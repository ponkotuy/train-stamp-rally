package queries

import models.Company

final case class CreateCompany(name: String) {
  def company = Company(0L, name)
}
