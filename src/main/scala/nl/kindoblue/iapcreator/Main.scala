package nl.kindoblue.iapcreator

import java.security.PrivateKey
import java.security.cert.X509Certificate

import org.bouncycastle.asn1.util.ASN1Dump
import org.clapper.argot.{ArgotConverters, ArgotParser}
import org.joda.time.DateTime



/**
 * Created by Stefano on 25/05/15.
 */

object Main {

  def main (args: Array[String]) {

    // create a sample receipt
    val now = new DateTime()
    val purchase1 = new Purchase(1, "nl.kindoblue.superfeature", "890000151620836", "",  now, now)
    val purchase2 = new Purchase(2, "nl.kindoblue.another", "890000151620856", "",  now, now)
    val receipt = new Receipt(ReceiptType.CustomSandbox, "com.kindoblue.nl", "1.0", List(purchase1, purchase2), None, None)

    // encode the receipt
    val message = ReceiptEncoder.encode(receipt, "not-used")


    // create certificates. The End cert and its private key to sign the receipt
    val certs = CertificateGenerator.generateTestCertificates("CN=Stefano", "CN=Intermediate", "CN=End cert")

    // get those certificates
    val (root : X509Certificate, intermediate: X509Certificate, endCert: X509Certificate, privateKey: PrivateKey) = certs

    // finally create the signed data
    val signedData = CertificateGenerator.createSignedData(message, root, intermediate, endCert, privateKey)

    val contentInfo = signedData.toASN1Structure

    println(ASN1Dump.dumpAsString(contentInfo, true))


    // http://software.clapper.org/argot/

    val parser = new ArgotParser("create-iap", preUsage=Some("Version 1.0"))

    import ArgotConverters._

    // implicit conversion
    val iterations = parser.option[Int](List("i", "iterations"), "n",
      "total iterations")

    println(iterations)
  }

}
