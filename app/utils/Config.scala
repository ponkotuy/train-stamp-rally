package utils

import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import play.api.Configuration

class Config(orig: Configuration) {
  lazy val googleMapsKey: Option[String] = orig.getString("google.maps.key")
  lazy val amazon = orig.getConfig("amazon").map(new AmazonConfig(_))
  lazy val mail: Option[String] = orig.getString("management.mail")
}

class AmazonConfig(config: Configuration) {
  val regionRaw: Option[String] = config.getString("region")
  val access: Option[String] = config.getString("access_key")
  val secret: Option[String] = config.getString("secret_key")

  lazy val credentials: Option[AWSCredentials] = for {
    a <- access
    s <- secret
  } yield new BasicAWSCredentials(a, s)

  lazy val region: Option[Regions] = regionRaw.map(Regions.fromName)

  def ses: Option[MyAmazonSES] = for {
    c <- credentials
    r <- region
  } yield new MyAmazonSES(c, r)
}
