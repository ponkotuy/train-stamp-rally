package utils

import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import play.api.Configuration

class Config(orig: Configuration) {
  lazy val googleMapsKey: Option[String] = orig.getString("google.maps.key")
  lazy val amazon = orig.getConfig("amazon").map(new AmazonConfig(_))
  lazy val amazonCredentials: Option[AWSCredentials] = for {
    a <- amazon
    access <- a.access
    secret <- a.secret
  } yield new BasicAWSCredentials(access, secret)
}

class AmazonConfig(config: Configuration) {
  val access: Option[String] = config.getString("access_key")
  val secret: Option[String] = config.getString("secret_key")
}
