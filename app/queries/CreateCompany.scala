package queries

import models.Company

case class CreateCompany(name: String) {
  def company = Company(0L, name)
}
