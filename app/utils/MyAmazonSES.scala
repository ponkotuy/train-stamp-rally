package utils

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model._

class MyAmazonSES(credentials: AWSCredentials, region: Regions) {
  lazy val client = new AmazonSimpleEmailServiceClient(credentials)
  client.withRegion(region)

  def send(mail: Mail): Unit = {
    val req = new SendEmailRequest()
      .withSource(mail.from)
      .withDestination(mail.dest)
      .withMessage(mail.message)
    client.sendEmail(req)
  }
}

case class Mail(dest: Destination, subject: Content, body: Body, from: String) {
  def message: Message = new Message().withSubject(subject).withBody(body)
}
